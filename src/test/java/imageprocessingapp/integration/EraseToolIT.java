package imageprocessingapp.integration;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.EraseTool;
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
 * Tests d'intégration pour l'outil de gomme (EraseTool).
 * 
 * Ces tests vérifient l'intégration entre le EraseTool, le Canvas
 * et l'ImageModel lors de l'effacement sur le canvas.
 */
class EraseToolIT {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    /**
     * Teste que l'outil de gomme efface le canvas.
     * 
     * Vérifie que :
     * - Le EraseTool peut être créé et configuré
     * - L'appel à onMousePressed efface le canvas (clearRect)
     * - L'outil fonctionne sans erreur (vérifié indirectement via un pixel voisin)
     * 
     * Note: Ce test vérifie indirectement le comportement en vérifiant qu'un pixel
     * voisin (non effacé) reste noir, confirmant ainsi que l'outil a été exécuté.
     */
    @Test
    void eraseClearsToTransparency() throws InterruptedException {
        AtomicReference<Boolean> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
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

            erase.setBrushSize(10); // Taille de gomme suffisante pour être visible
            
            // Prendre un snapshot avant l'effacement
            WritableImage beforeSnap = canvas.snapshot(null, null);
            Color beforeColor = beforeSnap.getPixelReader().getColor((int) x, (int) y);
            
            erase.onMousePressed(press, model);

            // Snapshot après l'effacement
            WritableImage afterSnap = canvas.snapshot(null, null);
            PixelReader reader = afterSnap.getPixelReader();
            
            // Vérifier que le pixel effacé a changé (opacité réduite ou transparent)
            Color afterColor = reader.getColor((int) x, (int) y);
            
            // Le pixel effacé devrait avoir une opacité réduite ou être différent
            // (clearRect rend transparent, donc l'opacité devrait être < 1.0)
            boolean pixelErased = afterColor.getOpacity() < beforeColor.getOpacity() || 
                                  !afterColor.equals(beforeColor);
            
            // Vérifier aussi qu'un pixel voisin (non effacé) est toujours noir
            Color nearby = reader.getColor((int) x + 15, (int) y);
            boolean nearbyUnchanged = nearby.equals(Color.BLACK) || 
                                      (nearby.getRed() < 0.1 && nearby.getGreen() < 0.1 && nearby.getBlue() < 0.1);
            
            // L'outil fonctionne si le pixel effacé a changé ET le pixel voisin est inchangé
            result.set(pixelErased && nearbyUnchanged);
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test did not complete in time");
        assertTrue(result.get(), 
                "Erase tool should clear the pixel (reduce opacity) and leave nearby pixels unchanged");
    }
}


