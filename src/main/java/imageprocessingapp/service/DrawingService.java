package imageprocessingapp.service;

import imageprocessingapp.model.ImageModel;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service centralisé pour la gestion du canvas de dessin.
 * 
 * Ce service encapsule toute la logique liée au canvas et à la composition d'images,
 * permettant de séparer les responsabilités entre les contrôleurs.
 */
public class DrawingService {
    
    private Canvas drawingCanvas;
    private ImageModel imageModel;
    private Runnable onCanvasModified;
    
    /**
     * Initialise le service avec le canvas et le modèle d'image.
     */
    public DrawingService(Canvas drawingCanvas, ImageModel imageModel) {
        this.drawingCanvas = drawingCanvas;
        this.imageModel = imageModel;
    }
    
    /**
     * Définit le callback appelé quand le canvas est modifié.
     * 
     * @param callback Le callback à appeler
     */
    public void setOnCanvasModified(Runnable callback) {
        this.onCanvasModified = callback;
    }
    
    /**
     * Notifie que le canvas a été modifié.
     */
    private void notifyCanvasModified() {
        if (onCanvasModified != null) {
            onCanvasModified.run();
        }
    }

    /**
     * Exécute une action sur le thread JavaFX de manière synchrone.
     * 
     * Cette méthode garantit que toute modification du canvas ou interaction avec l'UI 
     * se fait sur le thread d'application JavaFX. 
     * En JavaFX, toute opération qui modifie l'interface graphique DOIT être exécutée 
     * sur ce thread principal, sinon l'application peut crasher ou exposer des comportements non déterministes.
     *
     * @param action L'action à exécuter sur le thread UI JavaFX
     */
    private void runOnFxThreadSync(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try { action.run(); } finally { latch.countDown(); }
            });
            try { 
                latch.await(); 
            } catch (InterruptedException ignored) { 
                Thread.currentThread().interrupt(); 
            }
        }
    }
    
    /**
     * Configure le canvas pour le dessin.
     */
    public void setupCanvas() {
        runOnFxThreadSync(() -> {
            drawingCanvas.setMouseTransparent(false);
            
            // Configuration CSS pour forcer la transparence
            drawingCanvas.setStyle("-fx-background-color: transparent;");
            
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
            
            // Nettoyer et configurer la transparence
            gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
            
            // Configuration critique pour la transparence
            gc.setGlobalAlpha(1.0);
            gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
            
            // S'assurer que le canvas est transparent
            gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        });
    }
    
    /**
     * Crée un canvas par défaut (blanc si pas d'image, transparent si image chargée).
     */
    public void createDefaultCanvas() {
        runOnFxThreadSync(() -> {
            if (drawingCanvas != null) {
                GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
                
                // Nettoyer complètement le canvas
                gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                
                if (imageModel.getImage() == null) {
                    // Fond blanc seulement si aucune image n'est chargée
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                }
            }
        });
    }

    /**
     * Redimensionne le canvas pour correspondre à l'image chargée.
     */
    public void resizeCanvasToImage(Image image) {
        runOnFxThreadSync(() -> {
            if (image != null) {
                double[] dimensions = imageModel.calculateDisplayDimensions(800, 600);
                double displayWidth = dimensions[0];
                double displayHeight = dimensions[1];
                
                drawingCanvas.setWidth(displayWidth);
                drawingCanvas.setHeight(displayHeight);
                
                // IMPORTANT: Nettoyer complètement le canvas après redimensionnement
                GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, displayWidth, displayHeight);
                
                // Forcer la transparence en configurant les propriétés de blend
                gc.setGlobalAlpha(1.0);
            }
        });
    }
    
    /**
     * Génère une image composite résultat du dessin et de l'image affichée.
     * Cette opération doit impérativement s'exécuter sur le thread JavaFX : 
     * - Si on est déjà sur le thread JavaFX, on appelle directement la méthode du modèle.
     * - Sinon, on utilise Platform.runLater et une synchronisation pour obtenir le résultat.
     */
    public Image createCompositeImage() {
        if (Platform.isFxApplicationThread()) {
            return imageModel.createCompositeImage(drawingCanvas);
        } else {
            // atomic reference pour stocker le résultat de la création de l'image composite
            AtomicReference<Image> ref = new AtomicReference<>();
            // count down latch pour attendre la fin de la création de l'image composite
            CountDownLatch latch = new CountDownLatch(1);
            // exécuter la création de l'image composite sur le thread JavaFX
            Platform.runLater(() -> {
                try { ref.set(imageModel.createCompositeImage(drawingCanvas)); }
                finally { latch.countDown(); }
            });
            // attendre la fin de la création de l'image composite
            try { latch.await(); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            return ref.get();
        }
    }
    
    /**
     * Retourne le canvas de dessin.
     */
    public Canvas getDrawingCanvas() {
        return drawingCanvas;
    }
}