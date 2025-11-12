package imageprocessingapp.model.operations;

import imageprocessingapp.model.ImageModel;
import javafx.scene.image.WritableImage;
import java.util.Objects;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;

public final class SymmetryOperation implements Operation {

    public enum Axis { HORIZONTAL, VERTICAL }

    private final Axis axis;

    public SymmetryOperation(Axis axis) {
        this.axis = Objects.requireNonNull(axis);
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

        WritableImage flipped = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = flipped.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sourceX = (axis == Axis.VERTICAL) ? width - 1 - x : x;
                int sourceY = (axis == Axis.HORIZONTAL) ? height - 1 - y : y;
                writer.setColor(x, y, reader.getColor(sourceX, sourceY));
            }
        }

        imageModel.setImage(flipped);
        return flipped;
    }
}