package imageprocessingapp.model.operations;
import imageprocessingapp.model.ImageModel;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Objects;

/*
* Applique une rotation de 90 degrés sens horaire de l'image importée
 */

public class RotateOperation implements Operation {

    public enum Direction { CLOCKWISE, COUNTERCLOCKWISE }

    private final RotateOperation.Direction direction;

    public RotateOperation(RotateOperation.Direction direction) {
        this.direction = Objects.requireNonNull(direction);
    }

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

        // Image de format opposé
        WritableImage rotated = new WritableImage(height, width);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = rotated.getPixelWriter();

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                // Fonction de N^2 dans N^2 (x, y) -> (height - y, x)
                Color c = reader.getColor(x, y);
                if (direction == Direction.CLOCKWISE) {
                    writer.setColor(height - 1 - y, x, c);
                } else {
                    writer.setColor(y, width - 1 - x, c);
                }
            }
        }
        imageModel.setImage(rotated);
        return rotated;
    }
}