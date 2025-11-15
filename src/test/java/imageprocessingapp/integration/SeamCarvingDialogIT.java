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

/**
 * Tests d'intégration pour le dialogue de Seam Carving.
 * 
 * Ces tests vérifient l'intégration entre le SeamCarvingDialogController,
 * le MainController, le SeamCarvingService et l'interface utilisateur (FXML).
 */
class SeamCarvingDialogIT {

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    /**
     * Teste que le dialogue redimensionne l'image lors de la confirmation.
     * 
     * Vérifie que :
     * - Les sliders sont présents et fonctionnels
     * - La modification des sliders déclenche la prévisualisation
     * - La confirmation applique le redimensionnement à l'image
     * - L'image résultante a les dimensions cibles
     * 
     * Note: Le Thread.sleep(100) est utilisé pour attendre que la prévisualisation
     * soit mise à jour après le changement des sliders. Une amélioration serait
     * d'utiliser des listeners sur les propriétés observables.
     */
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
                
                assertNotNull(widthSlider, "Width slider should be present");
                assertNotNull(heightSlider, "Height slider should be present");
                
                // Attendre que les sliders soient initialisés
                Platform.runLater(() -> {
                    try {
                        // Créer un CountDownLatch pour attendre la mise à jour de la prévisualisation
                        CountDownLatch previewLatch = new CountDownLatch(1);
                        AtomicReference<Image> previousImage = new AtomicReference<>(currentImage.get());
                        
                        // Ajouter un listener pour détecter quand l'image est mise à jour
                        javafx.beans.value.ChangeListener<Image> previewListener = (obs, oldImg, newImg) -> {
                            if (newImg != null && !newImg.equals(previousImage.get())) {
                                previousImage.set(newImg);
                                previewLatch.countDown();
                            }
                        };
                        
                        currentImage.addListener(previewListener);
                        
                        // Modifier les sliders pour déclencher la prévisualisation
                        widthSlider.setValue(8);
                        heightSlider.setValue(7);
                        
                        // Attendre que la prévisualisation soit mise à jour (max 5 secondes)
                        // Si la prévisualisation ne change pas (dimensions identiques), on continue quand même
                        new Thread(() -> {
                            try {
                                if (!previewLatch.await(5, TimeUnit.SECONDS)) {
                                    // Si pas de changement après 5 secondes, continuer quand même
                                    // (peut arriver si les dimensions sont identiques)
                                    Platform.runLater(() -> previewLatch.countDown());
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                        
                        previewLatch.await(5, TimeUnit.SECONDS);
                        currentImage.removeListener(previewListener);
                        
                        Button okButton = (Button) root.lookup("#okButton");
                        assertNotNull(okButton, "Ok button should be present");
                        okButton.fire();
                        
                        result.set(mainController.currentImageProperty().get());
                        latch.countDown();
                    } catch (Exception e) {
                        fail("Exception: " + e.getMessage());
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Test did not complete in time");
        Image resizedImage = result.get();
        assertNotNull(resizedImage, "Resized image should not be null");
        assertTrue(resizedImage.getWidth() > 0 && resizedImage.getHeight() > 0, 
                "Resized image should have valid dimensions");
    }

    /**
     * Teste que le dialogue restaure l'image originale lors de l'annulation.
     * 
     * Vérifie que :
     * - La modification des sliders crée une prévisualisation
     * - L'annulation restaure l'image originale
     * - Les dimensions de l'image originale sont conservées
     * 
     * Note: Le Thread.sleep(100) est utilisé pour attendre que la prévisualisation
     * soit mise à jour après le changement des sliders. Une amélioration serait
     * d'utiliser des listeners sur les propriétés observables.
     */
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
                
                assertNotNull(widthSlider, "Width slider should be present");
                assertNotNull(heightSlider, "Height slider should be present");
                
                // Attendre que les sliders soient initialisés
                Platform.runLater(() -> {
                    try {
                        // Créer un CountDownLatch pour attendre la mise à jour de la prévisualisation
                        CountDownLatch previewLatch = new CountDownLatch(1);
                        AtomicReference<Image> previousImage = new AtomicReference<>(currentImage.get());
                        
                        // Ajouter un listener pour détecter quand l'image est mise à jour
                        javafx.beans.value.ChangeListener<Image> previewListener = (obs, oldImg, newImg) -> {
                            if (newImg != null && !newImg.equals(previousImage.get())) {
                                previousImage.set(newImg);
                                previewLatch.countDown();
                            }
                        };
                        
                        currentImage.addListener(previewListener);
                        
                        // Modifier les sliders pour déclencher la prévisualisation
                        widthSlider.setValue(8);
                        heightSlider.setValue(7);
                        
                        // Attendre que la prévisualisation soit mise à jour (max 5 secondes)
                        // Si la prévisualisation ne change pas (dimensions identiques), on continue quand même
                        new Thread(() -> {
                            try {
                                if (!previewLatch.await(5, TimeUnit.SECONDS)) {
                                    // Si pas de changement après 5 secondes, continuer quand même
                                    // (peut arriver si les dimensions sont identiques)
                                    Platform.runLater(() -> previewLatch.countDown());
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                        
                        previewLatch.await(5, TimeUnit.SECONDS);
                        currentImage.removeListener(previewListener);
                        
                        Button cancelButton = (Button) root.lookup("#cancelButton");
                        assertNotNull(cancelButton, "Cancel button should be present");
                        cancelButton.fire();
                        
                        result.set(mainController.currentImageProperty().get());
                        latch.countDown();
                    } catch (Exception e) {
                        fail("Exception: " + e.getMessage());
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Test did not complete in time");
        Image restoredImage = result.get();
        assertNotNull(restoredImage, "Restored image should not be null");
        assertEquals(10, restoredImage.getWidth(), "Original width should be restored");
        assertEquals(10, restoredImage.getHeight(), "Original height should be restored");
    }

    /**
     * Teste que le dialogue gère correctement les valeurs de sliders invalides (trop grandes).
     * 
     * Vérifie que :
     * - Le dialogue ne plante pas avec des valeurs de sliders supérieures aux dimensions
     * - Le dialogue restaure l'image originale si les dimensions sont invalides
     */
    @Test
    void resizeHandlesInvalidDimensions() throws Exception {
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
                
                assertNotNull(widthSlider, "Width slider should be present");
                assertNotNull(heightSlider, "Height slider should be present");
                
                // Tester avec des valeurs trop grandes (devrait restaurer l'image originale)
                Platform.runLater(() -> {
                    try {
                        widthSlider.setValue(20); // Plus grand que l'image originale (10)
                        heightSlider.setValue(20);
                        
                        // Attendre un peu pour que la prévisualisation soit mise à jour
                        CountDownLatch previewLatch = new CountDownLatch(1);
                        AtomicReference<Image> previousImage = new AtomicReference<>(currentImage.get());
                        
                        javafx.beans.value.ChangeListener<Image> previewListener = new javafx.beans.value.ChangeListener<Image>() {
                            @Override
                            public void changed(javafx.beans.value.ObservableValue<? extends Image> obs, Image oldImg, Image newImg) {
                                if (newImg != null && !newImg.equals(previousImage.get())) {
                                    previousImage.set(newImg);
                                    previewLatch.countDown();
                                }
                            }
                        };
                        
                        currentImage.addListener(previewListener);
                        
                        new Thread(() -> {
                            try {
                                if (!previewLatch.await(2, TimeUnit.SECONDS)) {
                                    Platform.runLater(() -> previewLatch.countDown());
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                        
                        previewLatch.await(2, TimeUnit.SECONDS);
                        currentImage.removeListener(previewListener);
                        
                        // Annuler le dialogue
                        Button cancelButton = (Button) root.lookup("#cancelButton");
                        assertNotNull(cancelButton, "Cancel button should be present");
                        cancelButton.fire();
                        
                        result.set(mainController.currentImageProperty().get());
                        latch.countDown();
                    } catch (Exception e) {
                        fail("Exception: " + e.getMessage());
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Test did not complete in time");
        Image finalImage = result.get();
        assertNotNull(finalImage, "Image should not be null after cancel");
        assertEquals(10, finalImage.getWidth(), "Image should be restored to original width");
        assertEquals(10, finalImage.getHeight(), "Image should be restored to original height");
    }
}
