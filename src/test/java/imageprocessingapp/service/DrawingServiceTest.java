package imageprocessingapp.service;

import imageprocessingapp.model.ImageModel;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DrawingServiceTest {

    private static volatile boolean jfxStarted = false;

    @BeforeAll
    static void initJavaFX() throws Exception {
        if (jfxStarted) return;
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX Platform failed to start in time");
        jfxStarted = true;
    }

    @Test
    void setupCanvas() {
        Canvas canvas = new Canvas(200, 100);
        ImageModel model = new ImageModel();
        DrawingService service = new DrawingService(canvas, model);

        assertDoesNotThrow(service::setupCanvas);
    }

    @Test
    void resizeCanvasToImage() {
        Canvas canvas = new Canvas(10, 10);
        ImageModel model = new ImageModel();
        WritableImage img = new WritableImage(400, 200); // 2:1
        model.setImage(img);
        DrawingService service = new DrawingService(canvas, model);

        service.resizeCanvasToImage(img);

        assertEquals(800.0, canvas.getWidth(), 1e-9);
        assertEquals(600.0 / 3.0 * 2.0, canvas.getHeight(), 1e-9); // should be 400.0
        assertEquals(400.0, canvas.getHeight(), 1e-9);
    }

    @Test
    void createDefaultCanvasWhite() throws InterruptedException {
        Canvas canvas = new Canvas(100, 100);
        ImageModel model = new ImageModel();
        DrawingService service = new DrawingService(canvas, model);
        service.setupCanvas();

        assertNull(model.getImage());
        service.createDefaultCanvas();

        // On attend que la snapshot soit créée sur le thread JavaFX
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<WritableImage> ref = new AtomicReference<>();
        
        // On crée la snapshot sur le thread JavaFX
        Platform.runLater(() -> {
            try {
                ref.set(canvas.snapshot(null, null));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX snapshot timed out");
        
        WritableImage snapshot = ref.get();        
        PixelReader reader = snapshot.getPixelReader();
        Color center = reader.getColor(50, 50);
        assertEquals(Color.WHITE, center);
    }

    @Test
    void createCompositeImageNoBase() {
        Canvas canvas = new Canvas(123, 77);
        ImageModel model = new ImageModel();
        DrawingService service = new DrawingService(canvas, model);
        service.setupCanvas();
        service.createDefaultCanvas();

        Image composite = service.createCompositeImage();
        assertNotNull(composite);
        assertEquals(123, composite.getWidth(), 1e-9);
        assertEquals(77, composite.getHeight(), 1e-9);
    }
}


