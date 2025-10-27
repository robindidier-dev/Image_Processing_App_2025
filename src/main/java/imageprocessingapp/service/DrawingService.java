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
        drawingCanvas.setStyle("-fx-background-color: transparent;");
        
        // Ajouter un listener pour détecter les modifications
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        // Note: JavaFX ne fournit pas d'événement direct pour les modifications de canvas
        // On utilisera plutôt les événements de souris dans le contrôleur
    }
    
    /**
     * Crée un canvas blanc par défaut pour dessiner sans image.
     */
    public void createDefaultCanvas() {
        if (drawingCanvas != null) {
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
            
            // Remplir avec un fond blanc seulement si aucune image n'est chargée
            if (imageModel.getImage() == null) {
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
            
            // Effacer le fond blanc pour laisser voir l'image
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, displayWidth, displayHeight);
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