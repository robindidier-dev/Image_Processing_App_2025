package imageprocessingapp.model.operations;
import imageprocessingapp.model.ImageModel;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Opération de rotation d'une image de 90 degrés dans le sens horaire ou antihoraire.
 * 
 * Cette classe implémente l'interface {@link Operation} pour appliquer une rotation
 * de 90 degrés sur une image. La rotation peut être effectuée dans le sens horaire
 * (CLOCKWISE) ou antihoraire (COUNTERCLOCKWISE).
 * 
 * Lors de la rotation, les dimensions de l'image sont inversées (largeur ↔ hauteur).
 */
public class RotateOperation implements Operation {

    /**
     * Direction de rotation possible.
     */
    public enum Direction { 
        /** Rotation dans le sens horaire (vers la droite) */
        CLOCKWISE, 
        /** Rotation dans le sens antihoraire (vers la gauche) */
        COUNTERCLOCKWISE 
    }

    private final RotateOperation.Direction direction;

    /**
     * Constructeur.
     * 
     * @param direction La direction de rotation (CLOCKWISE ou COUNTERCLOCKWISE)
     * @throws NullPointerException si direction est null
     */
    public RotateOperation(RotateOperation.Direction direction) {
        this.direction = Objects.requireNonNull(direction);
    }

    /**
     * Applique la rotation sur l'image du modèle.
     * 
     * La rotation de 90 degrés transforme les coordonnées selon les formules suivantes :
     * - Rotation horaire : (x, y) → (height - 1 - y, x)
     * - Rotation antihoraire : (x, y) → (y, width - 1 - x)
     * 
     * Les dimensions de l'image sont inversées après la rotation.
     * 
     * @param imageModel Le modèle contenant l'image à transformer
     * @return L'image transformée avec les dimensions inversées
     * @throws IllegalStateException si aucune image n'est chargée ou si l'image n'est pas modifiable
     */
    @Override
    public WritableImage apply(ImageModel imageModel) {
        if (!imageModel.hasImage()) {
            throw new IllegalStateException("Aucune image chargée");
        }

        WritableImage source = imageModel.getWritableImage();
        if (source == null) {
            throw new IllegalStateException("Image modifiable indisponible");
        }

        int width = (int) source.getWidth();
        int height = (int) source.getHeight();

        // Image de format opposé (largeur ↔ hauteur)
        WritableImage rotated = new WritableImage(height, width);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = rotated.getPixelWriter();

        // Parcourir tous les pixels de l'image source
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c = reader.getColor(x, y);
                if (direction == Direction.CLOCKWISE) {
                    // Rotation horaire : (x, y) → (height - 1 - y, x)
                    writer.setColor(height - 1 - y, x, c);
                } else {
                    // Rotation antihoraire : (x, y) → (y, width - 1 - x)
                    writer.setColor(y, width - 1 - x, c);
                }
            }
        }
        imageModel.setImage(rotated);
        return rotated;
    }
}