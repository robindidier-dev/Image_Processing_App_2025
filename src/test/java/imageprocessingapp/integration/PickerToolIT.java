package imageprocessingapp.integration;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.PickerTool;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
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
 * Tests d'intégration pour l'outil de pipette (PickerTool).
 * 
 * Ces tests vérifient l'intégration entre le PickerTool, l'ImageView,
 * le Canvas et la propriété de couleur sélectionnée.
 */
class PickerToolIT {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    /**
     * Teste que l'outil de pipette sélectionne la couleur depuis l'image.
     * 
     * Vérifie que :
     * - Le PickerTool peut être créé et configuré
     * - L'appel à onMousePressed sur un pixel de l'image met à jour la couleur sélectionnée
     * - La couleur sélectionnée correspond à la couleur du pixel cliqué
     */
    @Test
    void picksColorFromImage() {
        WritableImage image = new WritableImage(20, 20);
        image.getPixelWriter().setColor(5, 5, Color.GREEN);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);

        Canvas canvas = new Canvas(20, 20);
        SimpleObjectProperty<Color> selected = new SimpleObjectProperty<>(Color.BLACK);
        PickerTool tool = new PickerTool(imageView, canvas, selected);

        double x = 5, y = 5;
        MouseEvent press = new MouseEvent(MouseEvent.MOUSE_PRESSED,
                x, y, x, y,
                MouseButton.PRIMARY, 1,
                false, false, false, false,
                true, false, false, true,
                false, false, null);

        tool.onMousePressed(press, new ImageModel());
        assertEquals(Color.GREEN, selected.get());
    }

    /**
     * Teste que l'outil de pipette sélectionne la couleur depuis le canvas.
     * 
     * Vérifie que :
     * - Le PickerTool peut sélectionner la couleur depuis le canvas
     * - L'appel à onMousePressed sur un pixel du canvas met à jour la couleur sélectionnée
     * - La couleur sélectionnée correspond à la couleur du pixel cliqué sur le canvas
     */
    @Test
    void picksColorFromCanvas() throws InterruptedException {
        AtomicReference<Color> resultColor = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
        ImageView imageView = new ImageView();
        Canvas canvas = new Canvas(20, 20);
        canvas.getGraphicsContext2D().setFill(Color.BLUE);
        canvas.getGraphicsContext2D().fillRect(10, 10, 1, 1);

        SimpleObjectProperty<Color> selected = new SimpleObjectProperty<>(Color.BLACK);
        PickerTool tool = new PickerTool(imageView, canvas, selected);

        double x = 10, y = 10;
        MouseEvent press = new MouseEvent(MouseEvent.MOUSE_PRESSED,
                x, y, x, y,
                MouseButton.PRIMARY, 1,
                false, false, false, false,
                true, false, false, true,
                false, false, null);

        tool.onMousePressed(press, new ImageModel());
            resultColor.set(selected.get());
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test did not complete in time");
        assertEquals(Color.BLUE, resultColor.get());
    }
}


