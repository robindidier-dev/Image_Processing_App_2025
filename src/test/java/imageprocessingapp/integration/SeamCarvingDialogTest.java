package imageprocessingapp.integration;

import imageprocessingapp.controller.MainController;
import imageprocessingapp.controller.SeamCarvingDialogController;
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
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SeamCarvingDialogTest {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @Test
    void resizeOnConfirm() throws Exception {
        WritableImage baseImage = new WritableImage(10, 10);
        var pixelWriter = baseImage.getPixelWriter();
        
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                pixelWriter.setColor(x, y, Color.rgb((x * 255) / 10, (y * 255) / 10, 128));
            }
        }

        ImageModel imageModel = new ImageModel();
        imageModel.setImage(baseImage);

        MainController mainController = new MainController();
        ObjectProperty<Image> currentImage = mainController.currentImageProperty();
        currentImage.set(baseImage);

        AtomicReference<Image> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(SeamCarvingDialogController.class.getResource("/imageprocessingapp/dialogs/SeamCarvingDialog.fxml"));
                Parent root = loader.load();
                SeamCarvingDialogController controller = loader.getController();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));

                controller.setStage(stage);
                controller.setMainController(mainController);
                controller.setCurrentImage(currentImage);
                controller.setImageModel(imageModel);

                Slider widthSlider = (Slider) root.lookup("#widthSlider");
                Slider heightSlider = (Slider) root.lookup("#heightSlider");
                
                widthSlider.setValue(8);
                heightSlider.setValue(7);

                Thread.sleep(100);

                Button okButton = (Button) root.lookup("#okButton");
                okButton.fire();

                result.set(mainController.currentImageProperty().get());
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        Image resizedImage = result.get();
        assertNotNull(resizedImage);
        assertTrue(resizedImage.getWidth() > 0 && resizedImage.getHeight() > 0);
    }

    @Test
    void cancelRestoresOriginal() throws Exception {
        WritableImage baseImage = new WritableImage(10, 10);
        var pixelWriter = baseImage.getPixelWriter();
        
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                pixelWriter.setColor(x, y, Color.rgb(x * 25, y * 25, 128));
            }
        }

        ImageModel imageModel = new ImageModel();
        imageModel.setImage(baseImage);

        MainController mainController = new MainController();
        ObjectProperty<Image> currentImage = mainController.currentImageProperty();
        currentImage.set(baseImage);

        AtomicReference<Image> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(SeamCarvingDialogController.class.getResource("/imageprocessingapp/dialogs/SeamCarvingDialog.fxml"));
                Parent root = loader.load();
                SeamCarvingDialogController controller = loader.getController();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));

                controller.setStage(stage);
                controller.setMainController(mainController);
                controller.setCurrentImage(currentImage);
                controller.setImageModel(imageModel);

                Slider widthSlider = (Slider) root.lookup("#widthSlider");
                Slider heightSlider = (Slider) root.lookup("#heightSlider");
                
                widthSlider.setValue(8);
                heightSlider.setValue(7);

                Thread.sleep(100);

                Button cancelButton = (Button) root.lookup("#cancelButton");
                cancelButton.fire();

                result.set(mainController.currentImageProperty().get());
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        Image restoredImage = result.get();
        assertNotNull(restoredImage);
        assertEquals(10, restoredImage.getWidth());
        assertEquals(10, restoredImage.getHeight());
    }
}
