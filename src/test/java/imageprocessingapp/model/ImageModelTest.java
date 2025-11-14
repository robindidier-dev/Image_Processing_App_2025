package imageprocessingapp.model;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ImageModelTest {

    @Test
    void calculateDimensionsNoImage() {
        ImageModel model = new ImageModel();
        double[] dims = model.calculateDisplayDimensions(800, 600);
        assertArrayEquals(new double[]{800.0, 600.0}, dims, 1e-9);
    }

    @Test
    void setImage() {
        ImageModel model = new ImageModel();
        WritableImage img = new WritableImage(200, 100);
        model.setImage(img);

        assertTrue(model.hasImage());
        assertEquals(200, model.getWidth());
        assertEquals(100, model.getHeight());
        assertNotNull(model.getImage());
        assertNotNull(model.getWritableImage());
    }

    @Test
    void calculateDimensionsPreservesRatio() {
        ImageModel model = new ImageModel();
        WritableImage img = new WritableImage(400, 200); // ratio 2:1
        model.setImage(img);

        double[] dims1 = model.calculateDisplayDimensions(800, 600); // fit into 800x600
        assertEquals(800.0, dims1[0], 1e-9);
        assertEquals(400.0, dims1[1], 1e-9); // keep 2:1

        double[] dims2 = model.calculateDisplayDimensions(300, 100);
        assertEquals(200.0, dims2[0], 1e-9);
        assertEquals(100.0, dims2[1], 1e-9);
    }

    @Test
    void clearResetsState() {
        ImageModel model = new ImageModel();
        model.setImage(new WritableImage(50, 50));
        assertTrue(model.hasImage());

        model.clear();
        assertFalse(model.hasImage());
        assertEquals(0, model.getWidth());
        assertEquals(0, model.getHeight());
        assertNull(model.getImage());
        assertNull(model.getWritableImage());
    }

    @Test
    void pixelColorBounds() {
        ImageModel model = new ImageModel(new WritableImage(10, 10));
        assertNull(model.getPixelColor(-1, 0));
        assertNull(model.getPixelColor(0, -1));
        assertNull(model.getPixelColor(10, 0));
        assertNull(model.getPixelColor(0, 10));

        WritableImage writable = model.getWritableImage();
        writable.getPixelWriter().setColor(5, 5, Color.RED);
        assertEquals(Color.RED, writable.getPixelReader().getColor(5, 5));
        assertThrows(IndexOutOfBoundsException.class,
                () -> writable.getPixelWriter().setColor(11, 5, Color.BLUE));
    }
}
