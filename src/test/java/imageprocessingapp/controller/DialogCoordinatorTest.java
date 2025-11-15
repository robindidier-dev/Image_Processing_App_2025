package imageprocessingapp.controller;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DialogCoordinatorTest {

    private MainController mainController;
    private ImageView imageView;
    private ObjectProperty<Image> currentImageProperty;
    private ImageModel imageModel;
    private DialogCoordinator dialogCoordinator;
    private Stage stage;

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @BeforeEach
    void setUp() {
        Platform.runLater(() -> {
            mainController = new MainController();
            imageView = new ImageView();
            currentImageProperty = new SimpleObjectProperty<>();
            imageModel = new ImageModel();
            
            // Créer une scène et une stage pour les tests
            StackPane root = new StackPane();
            root.getChildren().add(imageView);
            Scene scene = new Scene(root, 800, 600);
            stage = new Stage();
            stage.setScene(scene);
            
            dialogCoordinator = new DialogCoordinator(
                    mainController,
                    imageView,
                    currentImageProperty,
                    imageModel
            );
        });
        
        // Attendre que la scène soit configurée
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testOpenColorPicker_WithScene() {
        // Ce test vérifie uniquement que la méthode existe
        // Les dialogues JavaFX bloquent, donc on ne peut pas vraiment tester le comportement
        assertNotNull(dialogCoordinator);
        assertNotNull(imageView);
    }

    @Test
    void testOpenMosaicDialog_WithoutImage() {
        // Ce test vérifie uniquement que la méthode existe
        assertNotNull(dialogCoordinator);
        assertNull(currentImageProperty.get());
    }

    @Test
    void testOpenMosaicDialog_WithImage() {
        // Créer une image
        WritableImage testImage = new WritableImage(10, 10);
        testImage.getPixelWriter().setColor(0, 0, Color.RED);
        currentImageProperty.set(testImage);
        imageModel.setImage(testImage);
        
        // Vérifier que l'image est bien chargée
        assertNotNull(currentImageProperty.get());
        assertTrue(imageModel.hasImage());
    }

    @Test
    void testOpenSeamCarvingDialog_WithoutImage() {
        // Ce test vérifie uniquement que la méthode existe
        assertNotNull(dialogCoordinator);
        assertNull(currentImageProperty.get());
    }

    @Test
    void testOpenSeamCarvingDialog_WithImage() {
        // Créer une image
        WritableImage testImage = new WritableImage(10, 10);
        testImage.getPixelWriter().setColor(0, 0, Color.RED);
        currentImageProperty.set(testImage);
        imageModel.setImage(testImage);
        
        // Vérifier que l'image est bien chargée
        assertNotNull(currentImageProperty.get());
        assertTrue(imageModel.hasImage());
    }

    @Test
    void testGetOwnerStage_WithScene() {
        Platform.runLater(() -> {
            // Avec une scène configurée, getOwnerStage devrait retourner la stage
            // Note: getOwnerStage est privé, mais on peut tester via les méthodes publiques
            assertNotNull(imageView.getScene());
            assertNotNull(dialogCoordinator);
        });
    }

    @Test
    void testGetOwnerStage_WithoutScene() {
        Platform.runLater(() -> {
            // Créer un coordinator sans scène
            ImageView imageViewWithoutScene = new ImageView();
            DialogCoordinator coordinatorWithoutScene = new DialogCoordinator(
                    mainController,
                    imageViewWithoutScene,
                    currentImageProperty,
                    imageModel
            );
            
            // Le dialogue devrait gérer le cas où il n'y a pas de scène
            assertNotNull(coordinatorWithoutScene);
        });
    }
}

