package imageprocessingapp.integration;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.service.DrawingService;
import imageprocessingapp.util.JavaFxTestInitializer;
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

/**
 * Tests d'intégration pour la composition d'images (DrawingService).
 * 
 * Ces tests vérifient l'intégration entre le DrawingService, le Canvas,
 * l'ImageModel et la création d'images composites (fond + canvas).
 */
class ImageCompositionIT {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    /**
     * Teste que la composition d'images crée une image composite correcte.
     * 
     * Vérifie que :
     * - Le DrawingService peut créer une image composite (fond + canvas)
     * - Le canvas est correctement superposé sur l'image de fond
     * - Les pixels du canvas (rouge) sont visibles dans l'image composite
     * - Les pixels de l'image de fond (bleu) sont visibles dans les zones non dessinées
     * 
     * Note: Ce test utilise des calculs de coordonnées pour mapper les pixels
     * du canvas vers l'image composite, ce qui peut être fragile si les dimensions changent.
     */
    @Test
    void compositeImageCanvasOverlay() throws InterruptedException {
        AtomicReference<Color> redPixel = new AtomicReference<>();
        AtomicReference<Color> bluePixel = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
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
            
            // Attendre que le redimensionnement soit terminé
            // Le canvas peut avoir été redimensionné, donc on dessine après le redimensionnement
            double canvasWidth = canvas.getWidth();
            double canvasHeight = canvas.getHeight();
            
            // Dessiner un pixel rouge au centre du canvas (après redimensionnement)
            double centerX = canvasWidth / 2.0;
            double centerY = canvasHeight / 2.0;
            canvas.getGraphicsContext2D().setFill(Color.RED);
            canvas.getGraphicsContext2D().fillRect(centerX, centerY, 1, 1);

            Image composite = service.createCompositeImage();
            assertNotNull(composite, "Composite image should not be null");
            
            PixelReader reader = composite.getPixelReader();
            int compositeWidth = (int) composite.getWidth();
            int compositeHeight = (int) composite.getHeight();
            
            // Vérifier que les dimensions sont correctes
            assertEquals(50, compositeWidth, "Composite width should match base image");
            assertEquals(50, compositeHeight, "Composite height should match base image");
            
            // Le pixel au centre devrait être rouge (dessin du canvas)
            // Utiliser les coordonnées exactes du centre (25, 25) pour éviter les calculs fragiles
            int centerPixelX = compositeWidth / 2;
            int centerPixelY = compositeHeight / 2;
            Color centerColor = reader.getColor(centerPixelX, centerPixelY);
            
            // Le pixel au centre devrait être rouge (ou proche du rouge)
            boolean isRed = centerColor.getRed() > 0.5 && 
                           centerColor.getGreen() < 0.5 && 
                           centerColor.getBlue() < 0.5;
            
            // Un pixel dans le coin (0,0) devrait être bleu (image de base)
            Color cornerColor = reader.getColor(0, 0);
            boolean isBlue = cornerColor.getBlue() > 0.5 && 
                           cornerColor.getRed() < 0.5 && 
                           cornerColor.getGreen() < 0.5;
            
            redPixel.set(centerColor);
            bluePixel.set(cornerColor);
            
            // Vérifier les couleurs
            assertTrue(isRed, 
                    String.format("Center pixel should be red, but was: R=%.2f G=%.2f B=%.2f", 
                            centerColor.getRed(), centerColor.getGreen(), centerColor.getBlue()));
            assertTrue(isBlue, 
                    String.format("Corner pixel should be blue, but was: R=%.2f G=%.2f B=%.2f", 
                            cornerColor.getRed(), cornerColor.getGreen(), cornerColor.getBlue()));
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test did not complete in time");
        // Les assertions sont déjà faites dans le Platform.runLater pour avoir de meilleurs messages d'erreur
        assertNotNull(redPixel.get(), "Red pixel should be set");
        assertNotNull(bluePixel.get(), "Blue pixel should be set");
    }
}


