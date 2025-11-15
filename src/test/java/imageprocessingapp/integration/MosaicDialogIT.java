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

/**
 * Tests d'intégration pour le dialogue de mosaïque.
 * 
 * Ces tests vérifient l'intégration entre le MosaicDialogController,
 * le MainController, le MosaicFilterService et l'interface utilisateur (FXML).
 */
class MosaicDialogIT {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    /**
     * Teste que le dialogue applique l'effet mosaïque lors de la confirmation.
     * 
     * Vérifie que :
     * - Le slider est présent et fonctionnel
     * - La modification du slider déclenche la prévisualisation
     * - La confirmation applique l'effet mosaïque à l'image
     * - L'image résultante a les mêmes dimensions mais des couleurs modifiées
     */
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

    /**
     * Teste que le dialogue gère correctement les valeurs de slider invalides.
     * 
     * Vérifie que :
     * - Le dialogue ne plante pas avec des valeurs de slider à 0
     * - Le dialogue peut être annulé même après modification du slider
     */
    @Test
    void mosaicDialogHandlesInvalidSliderValues() throws Exception {
        WritableImage baseImage = new WritableImage(10, 10);
        baseImage.getPixelWriter().setColor(0, 0, Color.RED);

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
                assertNotNull(slider, "Slider should be present");
                
                // Tester avec une valeur à 0 (devrait restaurer l'image originale)
                slider.setValue(0);
                
                // Annuler le dialogue
                Button cancelButton = (Button) root.lookup("#cancelButton");
                assertNotNull(cancelButton, "Cancel button should be present");
                cancelButton.fire();

                result.set(mainController.currentImageProperty().get());
            } catch (Exception e) {
                fail("Exception during MosaicDialog with invalid values: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test did not complete in time");
        Image finalImage = result.get();
        assertNotNull(finalImage, "Image should not be null after cancel");
    }
}

