package imageprocessingapp.integration;

import imageprocessingapp.controller.MosaicDialogController;
import imageprocessingapp.controller.MainController;
import imageprocessingapp.model.ImageModel;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MosaicDialogTest {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @Test
    void mosaicDialogAppliesMosaicOnConfirm() throws Exception {
        WritableImage baseImage = new WritableImage(2, 2);
        baseImage.getPixelWriter().setColor(0, 0, Color.RED);
        baseImage.getPixelWriter().setColor(1, 0, Color.GREEN);
        baseImage.getPixelWriter().setColor(0, 1, Color.BLUE);
        baseImage.getPixelWriter().setColor(1, 1, Color.BLACK);

        ImageModel imageModel = new ImageModel();
        imageModel.setImage(baseImage);

        MainController mainController = new MainController();
        ObjectProperty<Image> currentImage = mainController.currentImageProperty();
        currentImage.set(baseImage);

        AtomicReference<Image> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(MosaicDialogController.class.getResource("/imageprocessingapp/dialogs/MosaicDialog.fxml"));
                Parent root = loader.load();
                MosaicDialogController controller = loader.getController();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));

                controller.setStage(stage);
                controller.setMainController(mainController);
                controller.setCurrentImage(currentImage);
                controller.setImageModel(imageModel);

                Slider slider = (Slider) root.lookup("#mosaicSlider");
                assertNotNull(slider, "Slider should be present in the dialog");
                slider.setValue(1); // déclenche updatePreview avec 1 cellule

                Button okButton = (Button) root.lookup("#okButton");
                assertNotNull(okButton, "Ok button should be present in the dialog");
                okButton.fire(); // déclenche okPressed()

                result.set(mainController.currentImageProperty().get());
            } catch (Exception e) {
                fail("Exception during MosaicDialog workflow: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Mosaic dialog workflow did not finish in time");
        Image mosaicImage = result.get();
        assertNotNull(mosaicImage, "Mosaic image should not be null after confirmation");

        PixelReader reader = mosaicImage.getPixelReader();
        PixelReader originalReader = baseImage.getPixelReader();
        Color color = reader.getColor(0, 0);

        Color color11 = reader.getColor(1, 1);
        assertEquals(color.getRed(), color11.getRed(), 0.05);
        assertEquals(color.getGreen(), color11.getGreen(), 0.05);
        assertEquals(color.getBlue(), color11.getBlue(), 0.05);

        Color original = originalReader.getColor(0, 0);
        assertTrue(Math.abs(color.getRed() - original.getRed()) > 0.05
                || Math.abs(color.getGreen() - original.getGreen()) > 0.05
                || Math.abs(color.getBlue() - original.getBlue()) > 0.05,
            "Mosaic effect should change the pixel color compared to the original image");
    }
}

