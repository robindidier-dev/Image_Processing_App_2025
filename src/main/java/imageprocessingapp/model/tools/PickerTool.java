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
     * @param drawingCanvas Le Canvas contenant le dessin superposé (peut être null).
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

        // D'abord, couleur de l'image composite (image + dessin), via le modèle.
        Color color = sampleCompositeColor(event, imageModel);


        // Puis, on essaie de récupérer la couleur directement depuis le Canvas de dessin.
        if (color == null) {
            color = readColorFromCanvas(event);
        }

        // Enfin, couleur récupérée depuis l'image affichée dans l'ImageView seule.
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


            // Calcul des ratios de mise à l'échelle entre affichage et image réelle
            double viewWidth = drawingCanvas.getBoundsInParent().getWidth();
            double viewHeight = drawingCanvas.getBoundsInParent().getHeight();
            double canvasWidth = snapshot.getWidth();
            double canvasHeight = snapshot.getHeight();

            double scaleX = canvasWidth / viewWidth;
            double scaleY = canvasHeight / viewHeight;

            int x = (int) (event.getX() * scaleX);
            int y = (int) (event.getY() * scaleY);

            PixelReader reader = snapshot.getPixelReader();
            Color color = reader.getColor(x, y);
            return color;
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du canvas : " + e.getMessage());
            return null;
        }
    }

    /**
     * Lit une couleur depuis l'image affichée dans l'ImageView.
     */
    private Color readColorFromImage(MouseEvent event) {
        try {
            Image image = imageView.getImage();

            if (image == null) {
                return null;
            }


            // Calcul des ratios de mise à l'échelle entre affichage et image réelle
            double viewWidth = imageView.getBoundsInParent().getWidth();
            double viewHeight = imageView.getBoundsInParent().getHeight();
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();

            double scaleX = imageWidth / viewWidth;
            double scaleY = imageHeight / viewHeight;

            int imageX = (int) (event.getX() * scaleX);
            int imageY = (int) (event.getY() * scaleY);

            PixelReader reader = image.getPixelReader();

            // Vérification explicite des coordonnées pour éviter les erreurs d'index hors limites
            if (imageX < 0 || imageY < 0 || imageX >= imageWidth || imageY >= imageHeight) {
                return null;
            }
            return reader.getColor(imageX, imageY);
        } catch (Exception e) {
            // Gestion des exceptions
            System.out.println("Erreur lors de la lecture de l'image : " + e.getMessage());
            return null;
        }
    }

    /**
     * Lit la couleur de l'image composite (image + dessin) via le modèle.
     */
    private Color sampleCompositeColor(MouseEvent event, ImageModel imageModel) {

        // Vérifie d'abord si une image composite peut être générée
        boolean hasComposite = imageModelHasComposite(imageModel);
        if (!hasComposite) {
            return null;
        }
        if (drawingCanvas == null) {
            return null;
        }

        // délègue la lecture couleur sur cette image
        return readCompositeColorFromModel(event, imageModel);
    }

    /**
     * Vérifie si le modèle contient une image ou un dessin actif pour composer l'image composite.
     *
     * @param imageModel Le modèle à vérifier.
     * @return true si une image ou un dessin sont présents, false sinon.
     */
    private boolean imageModelHasComposite(ImageModel imageModel) {

        if (imageModel == null) {
            return false;
        }
        boolean hasImage = imageModel.getImage() != null;
        boolean hasCanvas = drawingCanvas != null;

        if (hasImage) {
            return true;
        }
        if (hasCanvas) {
            return true;
        }
        return false;
    }


    /**
     * Lit la couleur à partir de l'image composite générée par le modèle.
     * @param event L'événement de souris.
     * @param imageModel Le modèle contenant la méthode de composition.
     * @return La couleur du pixel composite ou null en cas d'erreur.
     */
    private Color readCompositeColorFromModel(MouseEvent event, ImageModel imageModel) {
        try {

            // Génère l'image composite image + dessin
            Image compositeImage = imageModel.createCompositeImage(drawingCanvas);

            double canvasWidth = drawingCanvas.getWidth();
            double canvasHeight = drawingCanvas.getHeight();
            double compositeWidth = compositeImage.getWidth();
            double compositeHeight = compositeImage.getHeight();

            double scaleX = compositeWidth / canvasWidth;
            double scaleY = compositeHeight / canvasHeight;

            int x = (int) (event.getX() * scaleX);
            int y = (int) (event.getY() * scaleY);

            PixelReader reader = compositeImage.getPixelReader();
            return reader.getColor(x, y);
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture de l'image composite : " + e.getMessage());
            return null;
        }
    }

    //Avec la pipette, les méthodes onMouseDragged et onMouseReleased sont inutiles :
    //La seule action sera le clic sur un pixel de l'image.

    @Override
    public void onMouseDragged(MouseEvent event, ImageModel imageModel) {
        // Pas d'action pour la pipette lors du drag
    }

    @Override
    public void onMouseReleased(MouseEvent event, ImageModel imageModel) {
        // Pas d'action pour la pipette lors du relâchement du clic
    }

    @Override
    public String getName() {
        return "Pipette";
    }
}
