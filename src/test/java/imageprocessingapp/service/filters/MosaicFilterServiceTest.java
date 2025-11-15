package imageprocessingapp.service.filters;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.filters.MosaicFilter;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MosaicFilterServiceTest {

    private MosaicFilterService service;
    private ImageModel imageModel;
    private WritableImage testImage;

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @BeforeEach
    void setUp() {
        service = new MosaicFilterService();
        imageModel = new ImageModel();
        
        testImage = new WritableImage(10, 10);
        var pixelWriter = testImage.getPixelWriter();
        
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                Color color = Color.rgb((x * 255) / 10, (y * 255) / 10, 128);
                pixelWriter.setColor(x, y, color);
            }
        }
        
        imageModel.setImage(testImage);
    }

    @Test
    void applyMosaic() {
        Image result = service.applyMosaic(imageModel, 5, MosaicFilter.MosaicSeedMode.RANDOM);
        
        assertNotNull(result);
        assertEquals(testImage.getWidth(), result.getWidth());
        assertEquals(testImage.getHeight(), result.getHeight());
    }

    @Test
    void applyMosaicWithNullImageModel() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.applyMosaic(null, 5, MosaicFilter.MosaicSeedMode.RANDOM);
        });
    }

    @Test
    void applyMosaicWithEmptyImageModel() {
        // Test le cas où imageModel n'est pas null mais n'a pas d'image
        ImageModel emptyModel = new ImageModel();
        assertThrows(IllegalArgumentException.class, () -> {
            service.applyMosaic(emptyModel, 5, MosaicFilter.MosaicSeedMode.RANDOM);
        });
    }

    @Test
    void applyMosaicWithNegativePointCount() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.applyMosaic(imageModel, -1, MosaicFilter.MosaicSeedMode.RANDOM);
        });
    }

    @Test
    void applyMosaicWithNullMode() {
        // Quand mode est null, devrait utiliser RANDOM par défaut
        Image result = service.applyMosaic(imageModel, 5, null);
        assertNotNull(result);
        assertEquals(testImage.getWidth(), result.getWidth());
        assertEquals(testImage.getHeight(), result.getHeight());
    }

    @Test
    void applyMosaicWithRegularGridMode() {
        Image result = service.applyMosaic(imageModel, 5, MosaicFilter.MosaicSeedMode.REGULAR_GRID);
        assertNotNull(result);
        assertEquals(testImage.getWidth(), result.getWidth());
        assertEquals(testImage.getHeight(), result.getHeight());
    }
}
