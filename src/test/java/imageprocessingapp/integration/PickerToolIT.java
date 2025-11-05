package imageprocessingapp.integration;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.PickerTool;
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

import static org.junit.jupiter.api.Assertions.*;

class PickerToolIT {

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

    @Test
    void picksColorFromCanvas() {
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
        assertEquals(Color.BLUE, selected.get());
    }
}


