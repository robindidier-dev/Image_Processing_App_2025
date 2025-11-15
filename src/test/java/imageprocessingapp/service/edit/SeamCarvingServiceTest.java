package imageprocessingapp.service.edit;

import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeamCarvingServiceTest {

    private SeamCarvingService service;
    private WritableImage testImage;

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @BeforeEach
    void setUp() {
        service = new SeamCarvingService();
        testImage = new WritableImage(10, 10);
        var pixelWriter = testImage.getPixelWriter();
        
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                Color color = Color.rgb((x * 255) / 10, (y * 255) / 10, 128);
                pixelWriter.setColor(x, y, color);
            }
        }
    }

    @Test
    void resize() {
        WritableImage result = service.resize(testImage, 8, 8);
        
        assertNotNull(result);
        assertEquals(8, result.getWidth());
        assertEquals(8, result.getHeight());
    }

    @Test
    void resizeWithNullSource() {
        assertThrows(NullPointerException.class, () -> {
            service.resize(null, 5, 5);
        });
    }

    @Test
    void resizeWithInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> service.resize(testImage, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> service.resize(testImage, 5, 0));
        assertThrows(IllegalArgumentException.class, () -> service.resize(testImage, -1, 5));
        assertThrows(IllegalArgumentException.class, () -> service.resize(testImage, 5, -1));
        assertThrows(IllegalArgumentException.class, () -> service.resize(testImage, 15, 5));
        assertThrows(IllegalArgumentException.class, () -> service.resize(testImage, 5, 15));
    }

    @Test
    void directionEnum() {
        // Tester que l'enum Direction existe et a les bonnes valeurs
        SeamCarvingService.Direction vertical = SeamCarvingService.Direction.VERTICAL;
        SeamCarvingService.Direction horizontal = SeamCarvingService.Direction.HORIZONTAL;
        
        assertNotNull(vertical);
        assertNotNull(horizontal);
        assertNotEquals(vertical, horizontal);
    }

    @Test
    void resizeOnlyWidth() {
        // Test le cas où on réduit seulement la largeur (verticalToRemove > 0, horizontalToRemove == 0)
        WritableImage result = service.resize(testImage, 8, 10);
        
        assertNotNull(result);
        assertEquals(8, result.getWidth());
        assertEquals(10, result.getHeight());
    }

    @Test
    void resizeOnlyHeight() {
        // Test le cas où on réduit seulement la hauteur (verticalToRemove == 0, horizontalToRemove > 0)
        WritableImage result = service.resize(testImage, 10, 8);
        
        assertNotNull(result);
        assertEquals(10, result.getWidth());
        assertEquals(8, result.getHeight());
    }

    @Test
    void resizeNoReduction() {
        // Test le cas où on ne réduit pas (verticalToRemove == 0, horizontalToRemove == 0)
        WritableImage result = service.resize(testImage, 10, 10);
        
        assertNotNull(result);
        assertEquals(10, result.getWidth());
        assertEquals(10, result.getHeight());
    }
}
