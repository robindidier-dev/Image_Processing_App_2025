package imageprocessingapp.integration;

import imageprocessingapp.controller.ColorPickerDialogController;
import imageprocessingapp.controller.MainController;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour le dialogue de sélection de couleur.
 * 
 * Ces tests vérifient l'intégration entre le ColorPickerDialogController,
 * le MainController et l'interface utilisateur (FXML).
 */
class ColorPickerDialogIT {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    /**
     * Teste que le dialogue met à jour la couleur sélectionnée lors de la confirmation.
     * 
     * Vérifie que :
     * - Le ColorPicker est initialisé avec la couleur actuelle
     * - La modification de la couleur et la confirmation mettent à jour la propriété selectedColor
     */
    @Test
    void colorPickerUpdatesColorOnConfirm() throws Exception {
        MainController mainController = new MainController();
        Color initialColor = Color.BLACK;
        mainController.selectedColorProperty().set(initialColor);

        AtomicReference<Color> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(ColorPickerDialogController.class.getResource("/imageprocessingapp/dialogs/ColorPickerDialog.fxml"));
                Parent root = loader.load();
                ColorPickerDialogController controller = loader.getController();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));

                controller.setStage(stage);
                controller.setMainController(mainController);

                ColorPicker colorPicker = (ColorPicker) root.lookup("#colorPicker");
                assertNotNull(colorPicker, "ColorPicker should be present in the dialog");
                
                // Vérifier que le ColorPicker est initialisé avec la couleur actuelle
                assertEquals(initialColor, colorPicker.getValue());

                // Changer la couleur
                Color newColor = Color.RED;
                colorPicker.setValue(newColor);

                Button okButton = (Button) root.lookup("#okButton");
                assertNotNull(okButton, "Ok button should be present in the dialog");
                okButton.fire();

                result.set(mainController.selectedColorProperty().get());
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        Color selectedColor = result.get();
        assertNotNull(selectedColor);
        assertEquals(Color.RED, selectedColor);
    }

    /**
     * Teste que le dialogue n'applique pas les modifications lors de l'annulation.
     * 
     * Vérifie que :
     * - La modification de la couleur suivie d'une annulation ne change pas la couleur originale
     * - La propriété selectedColor reste inchangée après l'annulation
     */
    @Test
    void colorPickerCancelDoesNotUpdateColor() throws Exception {
        MainController mainController = new MainController();
        Color initialColor = Color.BLUE;
        mainController.selectedColorProperty().set(initialColor);

        AtomicReference<Color> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(ColorPickerDialogController.class.getResource("/imageprocessingapp/dialogs/ColorPickerDialog.fxml"));
                Parent root = loader.load();
                ColorPickerDialogController controller = loader.getController();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));

                controller.setStage(stage);
                controller.setMainController(mainController);

                ColorPicker colorPicker = (ColorPicker) root.lookup("#colorPicker");
                colorPicker.setValue(Color.GREEN);

                Button cancelButton = (Button) root.lookup("#cancelButton");
                assertNotNull(cancelButton, "Cancel button should be present in the dialog");
                cancelButton.fire();

                result.set(mainController.selectedColorProperty().get());
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        Color selectedColor = result.get();
        assertNotNull(selectedColor);
        assertEquals(initialColor, selectedColor, "Color should remain unchanged after cancel");
    }
}

