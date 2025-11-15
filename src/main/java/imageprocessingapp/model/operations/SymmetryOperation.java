package imageprocessingapp.model.operations;

import imageprocessingapp.model.ImageModel;
import javafx.scene.image.WritableImage;
import java.util.Objects;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;

/**
 * Opération de symétrie (miroir) d'une image selon un axe horizontal ou vertical.
 * 
 * Cette classe implémente l'interface {@link Operation} pour appliquer une symétrie
 * sur une image. La symétrie peut être effectuée selon un axe horizontal (miroir horizontal)
 * ou vertical (miroir vertical).
 * 
 * Les dimensions de l'image restent inchangées après la symétrie.
 */
public final class SymmetryOperation implements Operation {

    /**
     * Axe de symétrie possible.
     */
    public enum Axis { 
        /** Symétrie horizontale (retournement vertical) */
        HORIZONTAL, 
        /** Symétrie verticale (retournement horizontal) */
        VERTICAL 
    }

    private final Axis axis;

    /**
     * Constructeur.
     * 
     * @param axis L'axe de symétrie (HORIZONTAL ou VERTICAL)
     * @throws NullPointerException si axis est null
     */
    public SymmetryOperation(Axis axis) {
        this.axis = Objects.requireNonNull(axis);
    }

    /**
     * Applique la symétrie sur l'image du modèle.
     * 
     * La symétrie transforme les coordonnées selon les formules suivantes :
     * - Symétrie horizontale : (x, y) → (x, height - 1 - y)
     * - Symétrie verticale : (x, y) → (width - 1 - x, y)
     * 
     * Les dimensions de l'image restent inchangées après la symétrie.
     * 
     * @param imageModel Le modèle contenant l'image à transformer
     * @return L'image transformée avec les mêmes dimensions
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

        WritableImage flipped = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = flipped.getPixelWriter();

        // Parcourir tous les pixels de l'image source
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Calculer les coordonnées source selon l'axe de symétrie
                int sourceX = (axis == Axis.VERTICAL) ? width - 1 - x : x;
                int sourceY = (axis == Axis.HORIZONTAL) ? height - 1 - y : y;
                writer.setColor(x, y, reader.getColor(sourceX, sourceY));
            }
        }

        imageModel.setImage(flipped);
        return flipped;
    }
}