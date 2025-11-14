package imageprocessingapp.service.edit;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.edit.SeamCarver;
import imageprocessingapp.model.operations.RotateOperation;
import imageprocessingapp.model.structures.EnergyCalculator;
import javafx.scene.image.WritableImage;

import java.util.List;
import java.util.Objects;

/**
 * Service haut-niveau pour l'algorithme de Seam Carving.
 */
public class SeamCarvingService {

    public enum Direction {
        VERTICAL,
        HORIZONTAL
    }

    private final EnergyCalculator energyCalculator;
    private final SeamCarver seamCarver;

    public SeamCarvingService() {
        this.energyCalculator = new EnergyCalculator();
        this.seamCarver = new SeamCarver();
    }

    public WritableImage resize(WritableImage source, int targetWidth, int targetHeight) {
        Objects.requireNonNull(source, "source");

        int currentWidth = (int) source.getWidth();
        int currentHeight = (int) source.getHeight();

        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Les dimensions cibles doivent être strictement positives.");
        }
        if (targetWidth > currentWidth || targetHeight > currentHeight) {
            throw new IllegalArgumentException("Le Seam Carving ne peut qu'amoindrir l'image.");
        }

        int verticalToRemove = currentWidth - targetWidth;
        int horizontalToRemove = currentHeight - targetHeight;

        WritableImage working = clone(source);

        if (verticalToRemove > 0) {
            working = removeVerticalSeams(working, verticalToRemove);
        }
        if (horizontalToRemove > 0) {
            working = removeHorizontalSeams(working, horizontalToRemove);
        }

        return working;
    }

    private WritableImage removeVerticalSeams(WritableImage image, int count) {
        ImageModel model = new ImageModel(image);
        return seamCarver.resizeOptimized(model, count);
    }

    private WritableImage removeHorizontalSeams(WritableImage image, int count) {
        WritableImage rotated = rotate(image, RotateOperation.Direction.CLOCKWISE);
        WritableImage reduced = removeVerticalSeams(rotated, count);
        return rotate(reduced, RotateOperation.Direction.COUNTERCLOCKWISE);
    }

    private WritableImage rotate(WritableImage image, RotateOperation.Direction direction) {
        ImageModel model = new ImageModel(image);
        return new RotateOperation(direction).apply(model);
    }

    private WritableImage clone(WritableImage source) {
        ImageModel model = new ImageModel(source);
        return model.getWritableImage();
    }
}