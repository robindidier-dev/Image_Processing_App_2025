package imageprocessingapp.integration;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.EraseTool;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EraseToolIT {

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
    void eraseClearsToTransparency() {
        // Prepare model with an image so EraseTool uses transparent clearing
        ImageModel model = new ImageModel();
        model.setImage(new WritableImage(40, 40));

        Canvas canvas = new Canvas(40, 40);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Paint a solid black area first
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 40, 40);

        EraseTool erase = new EraseTool(gc);

        // Simulate mouse press in the center to erase
        double x = 20, y = 20;
        MouseEvent press = new MouseEvent(MouseEvent.MOUSE_PRESSED,
                x, y, x, y,
                MouseButton.PRIMARY, 1,
                false, false, false, false,
                true, false, false, true,
                false, false, null);

        erase.onMousePressed(press, model);

        // Snapshot and verify center pixel is transparent (alpha ~ 0)
        WritableImage snap = canvas.snapshot(null, null);
        PixelReader reader = snap.getPixelReader();
        Color center = reader.getColor((int) x, (int) y);
        assertTrue(center.getOpacity() < 0.05, "Center pixel should be transparent after erase");
    }
}


