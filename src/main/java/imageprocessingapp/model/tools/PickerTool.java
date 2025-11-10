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
        // Première étape : récupération des coordonnées de l'endroit où l'on clique

        // On récupère l'image
        Image image = imageView.getImage();

        // Vérification que l'image n'est pas nulle
        if (image != null) {
            // Cependant, il faut convertir les coordonnées du clic souris à la vraie échelle de l'image affichée.
            // -> nécessaire si l'image affichée est redimensionnée dans l'ImageView par rapport à sa taille originale.

            double viewWidth = imageView.getBoundsInParent().getWidth();
            double viewHeight = imageView.getBoundsInParent().getHeight();
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();

            double scaleX = imageWidth / viewWidth;
            double scaleY = imageHeight / viewHeight;

            int imageX = (int) (event.getX() * scaleX); // peut-être différent de event.getX() si l'image est redimensionnée
            int imageY = (int) (event.getY() * scaleY);

            // Deuxième étape : on veut récupérer la couleur de cet endroit
            // On utilise PixelReader
            PixelReader reader = image.getPixelReader();
            if (reader != null && imageX >= 0 && imageY >= 0 && imageX < imageWidth && imageY < imageHeight) { // <=> on a cliqué sur un pixel de la photo et non ailleurs
                Color color = reader.getColor(imageX, imageY);
                selectedColor.set(color); // méthode de la classe ObjectProperty
                // met à jour la valeur et notifient tous les listeners liés à cette propriété
                return;
            }
        }

        // si pas d'image, lire depuis le canvas
        if (drawingCanvas != null) {
            int canvasX = (int) event.getX();
            int canvasY = (int) event.getY();
            Color color = readColorFromCanvas(canvasX, canvasY);
            if (color != null) {
                selectedColor.set(color);
            }
        }
    }

    /**
     * Lit une couleur depuis le Canvas à la position donnée.
     */
    private Color readColorFromCanvas(int x, int y) {
        try {
            // Capturer le contenu du canvas comme image
            WritableImage snapshot = drawingCanvas.snapshot(null, null);
            
            // Vérifier les limites
            if (x >= 0 && y >= 0 && x < snapshot.getWidth() && y < snapshot.getHeight()) {
                PixelReader reader = snapshot.getPixelReader();
                return reader.getColor(x, y);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du canvas : " + e.getMessage());
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
