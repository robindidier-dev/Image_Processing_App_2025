package imageprocessingapp.model.operations;

import imageprocessingapp.model.ImageModel;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Opération de découpe (crop) d'une image selon une zone rectangulaire définie.
 */
public class CropOperation implements Operation {

    // Zone de découpe en coordonnées image (x, y, largeur, hauteur)
    private Rectangle2D cropArea;

    /**
     * Crée une opération de crop avec la zone spécifiée.
     *
     * @param cropArea Zone rectangulaire à extraire de l'image
     */
    public CropOperation(Rectangle2D cropArea) {
        this.cropArea = cropArea;
    }

    /**
     * Applique le crop sur l'image du modèle.
     *
     * @param imageModel Le modèle contenant l'image à cropper
     * @return Une nouvelle image contenant uniquement la zone croppée, ou null si pas d'image
     */
    @Override
    public WritableImage apply(ImageModel imageModel) {
        Image image = imageModel.getImage();
        if (image == null) return null;

        // Extraire les coordonnées et dimensions de la zone de crop
        int upperLeftX = (int) cropArea.getMinX();
        int upperLeftY = (int) cropArea.getMinY();
        int newWidth = (int) cropArea.getWidth();
        int newHeight = (int) cropArea.getHeight();

        // Créer une nouvelle image à partir de la zone sélectionnée
        PixelReader reader = image.getPixelReader();
        WritableImage croppedImage = new WritableImage(reader, upperLeftX, upperLeftY, newWidth, newHeight);

        return croppedImage;
    }


    // Getter - Setter pour modifier la zone de crop si besoin

    public Rectangle2D getCropArea() {
        return cropArea;
    }

    public void setCropArea(Rectangle2D cropArea) {
        this.cropArea = cropArea;
    }
}
