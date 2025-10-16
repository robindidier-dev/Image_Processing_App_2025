package imageprocessingapp.model.tools;

import imageprocessingapp.model.ImageModel;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class PickerTool implements Tool {

    /**
     * Permet de récupérer la couleur du pixel sous la souris.
     */

    private ImageView imageView;
    private ObjectProperty<Color> selectedColor;


    //le constructeur prend l'imageView sur laquelle lire la couleur, et la variable selectedColor qu'on va mettre à jour
    public PickerTool(ImageView imageView, ObjectProperty<Color> selectedColor) {
        this.imageView = imageView;
        this.selectedColor = selectedColor;
    }


    @Override
    public void onMousePressed(MouseEvent event, ImageModel imageModel) {

        // On récupère l'image
        Image image = imageView.getImage();

        if (image != null) {
            // Première étape : récupération des coordonnées de l'endroit où l'on clique
            int x = (int) event.getX();
            int y = (int) event.getY();

            // Deuxième étape : on veut récupérer la couleur de cet endroit
            // On utilise PixelReader
            PixelReader reader = image.getPixelReader();
            if (reader != null && x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) { // <=> on a cliqué sur un pixel de la photo et non ailleurs
                Color color = reader.getColor(x, y);
                selectedColor.set(color); //méthode de la classe ObjectProperty
                // met à jour la valeur et notifient tous les listeners liés à cette propriété
            }
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
