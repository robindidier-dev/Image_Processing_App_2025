package imageprocessingapp.model.tools;

import imageprocessingapp.model.ImageModel;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

/**
 * Outil pipette pour sélectionner une couleur depuis l'image.
 * 
 * Cet outil permet à l'utilisateur de cliquer sur un pixel de l'image
 * pour récupérer sa couleur et la définir comme couleur active.
 * 
 * Pattern Strategy : implémente l'interface Tool pour le comportement pipette.
 */
public class PickerTool implements Tool {

    /**
     * ImageView sur laquelle lire la couleur des pixels.
     * Utilisée pour accéder à l'image et lire les couleurs des pixels.
     */
    private final ImageView imageView;

    /**
     * Canvas sur lequel dessiner.
     * Utilisé pour accéder au canvas et dessiner sur l'image.
     */
    private final Canvas drawingCanvas;
    
    /**
     * Propriété observable contenant la couleur sélectionnée.
     * Cette propriété sera liée à la couleur active dans MainController.
     */
    private final ObjectProperty<Color> selectedColor;

    /**
     * Constructeur de l'outil pipette.
     * 
     * @param imageView L'ImageView sur laquelle lire la couleur des pixels
     * @param selectedColor La propriété observable contenant la couleur sélectionnée
     */
    public PickerTool(ImageView imageView, Canvas drawingCanvas, ObjectProperty<Color> selectedColor) {
        this.imageView = imageView;
        this.drawingCanvas = drawingCanvas;
        this.selectedColor = selectedColor;
    }

    /**
     * Gère le clic de souris pour sélectionner une couleur.
     * 
     * @param event L'événement de souris contenant les coordonnées du clic
     * @param imageModel Le modèle de l'image (non utilisé pour cet outil)
     */
    @Override
    public void onMousePressed(MouseEvent event, ImageModel imageModel) {
        Color color = sampleCompositeColor(event, imageModel);

        if (color == null) {
            color = readColorFromCanvas(event);
        }
        if (color == null) {
            color = readColorFromImage(event);
        }

        if (color != null) {
            selectedColor.set(color);
        }
    }

    /**
     * Lit une couleur depuis le Canvas à la position donnée.
     */
    private Color readColorFromCanvas(MouseEvent event) {
        if (drawingCanvas == null) {
            return null;
        }

        try {
            // Capturer le contenu du canvas comme image
            WritableImage snapshot = drawingCanvas.snapshot(null, null);

            double viewWidth = drawingCanvas.getBoundsInParent().getWidth();
            double viewHeight = drawingCanvas.getBoundsInParent().getHeight();
            double canvasWidth = snapshot.getWidth();
            double canvasHeight = snapshot.getHeight();

            if (viewWidth <= 0 || viewHeight <= 0) {
                return null;
            }

            double scaleX = canvasWidth / viewWidth;
            double scaleY = canvasHeight / viewHeight;

            int x = (int) (event.getX() * scaleX);
            int y = (int) (event.getY() * scaleY);
            
            // Vérifier les limites
            if (x >= 0 && y >= 0 && x < canvasWidth && y < canvasHeight) {
                PixelReader reader = snapshot.getPixelReader();
                if (reader == null) {
                    return null;
                }
                Color color = reader.getColor(x, y);
                return color.getOpacity() > 0 ? color : null;
            }
            return null;
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du canvas : " + e.getMessage());
            return null;
        }
    }

    /**
     * Lit une couleur depuis l'image affichée dans l'ImageView.
     */
    private Color readColorFromImage(MouseEvent event) {
        Image image = imageView.getImage();

        if (image == null) {
            return null;
        }

        double viewWidth = imageView.getBoundsInParent().getWidth();
        double viewHeight = imageView.getBoundsInParent().getHeight();
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        if (viewWidth <= 0 || viewHeight <= 0) {
            return null;
        }

        double scaleX = imageWidth / viewWidth;
        double scaleY = imageHeight / viewHeight;

        int imageX = (int) (event.getX() * scaleX);
        int imageY = (int) (event.getY() * scaleY);

        PixelReader reader = image.getPixelReader();
        if (reader != null && imageX >= 0 && imageY >= 0 && imageX < imageWidth && imageY < imageHeight) {
            return reader.getColor(imageX, imageY);
        }

        return null;
    }

    /**
     * Lit la couleur de l'image composite (image + dessin) via le modèle.
     */
    private Color sampleCompositeColor(MouseEvent event, ImageModel imageModel) {
        if (!imageModelHasComposite(imageModel) || drawingCanvas == null) {
            return null;
        }

        return readCompositeColorFromModel(event, imageModel);
    }

    private boolean imageModelHasComposite(ImageModel imageModel) {
        return imageModel != null && (imageModel.getImage() != null || drawingCanvas != null);
    }

    private Color readCompositeColorFromModel(MouseEvent event, ImageModel imageModel) {
        try {
            Image compositeImage = imageModel.createCompositeImage(drawingCanvas);
            if (compositeImage == null) {
                return null;
            }

            double canvasWidth = drawingCanvas.getWidth();
            double canvasHeight = drawingCanvas.getHeight();
            double compositeWidth = compositeImage.getWidth();
            double compositeHeight = compositeImage.getHeight();

            if (canvasWidth <= 0 || canvasHeight <= 0 || compositeWidth <= 0 || compositeHeight <= 0) {
                return null;
            }

            double scaleX = compositeWidth / canvasWidth;
            double scaleY = compositeHeight / canvasHeight;

            int x = (int) (event.getX() * scaleX);
            int y = (int) (event.getY() * scaleY);

            if (x < 0 || y < 0 || x >= compositeWidth || y >= compositeHeight) {
                return null;
            }

            PixelReader reader = compositeImage.getPixelReader();
            return reader != null ? reader.getColor(x, y) : null;
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture de l'image composite : " + e.getMessage());
            return null;
        }
    }

    //Avec la pipette, les méthodes onMouseDragged et onMouseReleased sont inutiles :
    //La seule action sera le clic sur un pixel de l'image.

    @Override
    public void onMouseDragged(MouseEvent event, ImageModel imageModel) {

    }

    @Override
    public void onMouseReleased(MouseEvent event, ImageModel imageModel) {

    }

    @Override
    public String getName() {
        return "Pipette";
    }
}
