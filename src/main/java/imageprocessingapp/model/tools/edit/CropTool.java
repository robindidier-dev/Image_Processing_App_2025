package imageprocessingapp.model.tools.edit;

import imageprocessingapp.service.DrawingService;
import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.Tool;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/**
 * Outil de sélection rectangulaire pour le crop.
 * Affiche un masque d'opacité en temps réel pendant la sélection.
 */
public class CropTool implements Tool {

    // Coordonnées de début et fin de sélection
    private double startX = -1, startY = -1;
    private double currentX = -1, currentY = -1;

    // Zone de crop résultante en coordonnées d'affichage
    private Rectangle2D cropArea;

    // Références externes
    private ImageView imageView;
    private Canvas maskCanvas;
    private DrawingService drawingService;

    /**
     * Initialise le point de départ de la sélection.
     */
    @Override
    public void onMousePressed(MouseEvent event, ImageModel imageModel) {
        startX = event.getX();
        startY = event.getY();
        currentX = startX;
        currentY = startY;
        cropArea = null;
    }

    /**
     * Met à jour la sélection et affiche le masque d'opacité en temps réel.
     */
    @Override
    public void onMouseDragged(MouseEvent event, ImageModel imageModel) {
        currentX = event.getX();
        currentY = event.getY();

        if (drawingService != null) {
            // Calculer la zone rectangulaire sélectionnée
            double x = Math.min(startX, currentX);
            double y = Math.min(startY, currentY);
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);
            Rectangle2D selection = new Rectangle2D(x, y, width, height);

            // Afficher le masque d'opacité
            drawingService.drawOpacityMask(selection);
        }
    }

    /**
     * Finalise la sélection et efface le masque d'opacité.
     * Stocke la zone de crop en coordonnées d'affichage.
     */
    @Override
    public void onMouseReleased(MouseEvent event, ImageModel imageModel) {
        // Effacer le masque d'opacité
        if (drawingService != null) {
            drawingService.drawOpacityMask(null);
        }

        if (maskCanvas != null) {
            GraphicsContext gc = maskCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, maskCanvas.getWidth(), maskCanvas.getHeight());
        }

        // Calculer la zone de crop finale (gérer tous les sens de drag)
        double x = Math.min(startX, currentX);
        double y = Math.min(startY, currentY);
        double width = Math.abs(currentX - startX);
        double height = Math.abs(currentY - startY);

        // Stocker en coordonnées d'affichage (pas de scale appliqué ici, le scale est géré plus tard dans le mainController)
        cropArea = new Rectangle2D(x, y, width, height);
    }

    @Override
    public String getName() {
        return "crop";
    }


    // ===== GETTERS / SETTERS =====

    public Rectangle2D getCropArea() {
        return cropArea;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setMaskCanvas(Canvas maskCanvas) {
        this.maskCanvas = maskCanvas;
    }

    public void setDrawingService(DrawingService drawingService) {
        this.drawingService = drawingService;
    }
}
