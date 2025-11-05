package imageprocessingapp.integration;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.service.DrawingService;
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

import static org.junit.jupiter.api.Assertions.*;

class ImageCompositionIT {

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
    void compositeImageCanvasOverlay() {
        // Base image 50x50 blue
        WritableImage base = new WritableImage(50, 50);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                base.getPixelWriter().setColor(x, y, Color.BLUE);
            }
        }

        ImageModel model = new ImageModel();
        model.setImage(base);

        Canvas canvas = new Canvas(50, 50);
        DrawingService service = new DrawingService(canvas, model);
        service.setupCanvas();
        service.resizeCanvasToImage(base);

        // Draw a red pixel at (10,10) on canvas
        canvas.getGraphicsContext2D().setFill(Color.RED);
        canvas.getGraphicsContext2D().fillRect(10, 10, 1, 1);

        Image composite = service.createCompositeImage();
        PixelReader reader = composite.getPixelReader();

        assertEquals(Color.RED, reader.getColor(10, 10));
        assertEquals(Color.BLUE, reader.getColor(0, 0));
    }
}


