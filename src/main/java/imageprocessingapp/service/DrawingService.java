package imageprocessingapp.service;

import imageprocessingapp.model.ImageModel;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

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
     * Configure le canvas pour le dessin.
     */
    public void setupCanvas() {
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
    }
    
    /**
     * Crée un canvas par défaut (blanc si pas d'image, transparent si image chargée).
     */
    public void createDefaultCanvas() {
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
    }

    /**
     * Redimensionne le canvas pour correspondre à l'image chargée.
     */
    public void resizeCanvasToImage(Image image) {
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
    }
    
    /**
     * Crée une image composite pour la sauvegarde.
     */
    public Image createCompositeImage() {
        return imageModel.createCompositeImage(drawingCanvas);
    }
    
    /**
     * Retourne le canvas de dessin.
     */
    public Canvas getDrawingCanvas() {
        return drawingCanvas;
    }
}