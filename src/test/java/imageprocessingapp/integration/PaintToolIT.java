package imageprocessingapp.integration;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.PaintTool;
import imageprocessingapp.util.JavaFxTestInitializer;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour l'outil de peinture (PaintTool).
 * 
 * Ces tests vérifient l'intégration entre le PaintTool, le Canvas
 * et l'ImageModel lors du dessin sur le canvas.
 */
class PaintToolIT {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    /**
     * Teste que l'outil de peinture dessine sur le canvas.
     * 
     * Vérifie que :
     * - Le PaintTool peut être créé et configuré
     * - L'appel à onMousePressed dessine sur le canvas
     * - Le pixel au point de clic est peint (opacité > 0.5)
     */
    @Test
    void paintDrawsOnCanvas() throws InterruptedException {
        AtomicReference<Boolean> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            Canvas canvas = new Canvas(40, 40);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            PaintTool tool = new PaintTool(gc);
            tool.setPaintColor(Color.RED);
            tool.setBrushSize(5);

            double x = 20, y = 20;
            MouseEvent press = new MouseEvent(MouseEvent.MOUSE_PRESSED,
                    x, y, x, y,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    true, false, false, true,
                    false, false, null);

            tool.onMousePressed(press, new ImageModel());

            WritableImage snap = canvas.snapshot(null, null);
            PixelReader reader = snap.getPixelReader();
            Color center = reader.getColor((int) x, (int) y);
            result.set(center.getOpacity() > 0.5);
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test did not complete in time");
        assertTrue(result.get(), "Center pixel should be painted");
    }
}


