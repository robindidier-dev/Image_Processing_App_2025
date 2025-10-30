package imageprocessingapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import imageprocessingapp.controller.MainController;

/**
 * Point d'entrée principal de l'application de traitement d'image.
 * Cette classe hérite de javafx.application.Application et constitue le point de départ
 * de l'application JavaFX. Elle initialise la fenêtre principale et charge l'interface
 * utilisateur définie dans MainView.fxml.
 */

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger l'interface utilisateur depuis le fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/imageprocessingapp/view/MainView.fxml"));
        Parent root = loader.load();
        
        // Récupérer le contrôleur associé à la vue
        MainController controller = loader.getController();

        // Configurer et afficher la fenêtre principale
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Image Processing App");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        
        // Configurer les raccourcis clavier au niveau de la scène
        setupGlobalKeyboardShortcuts(scene, controller);
        
        primaryStage.show();

        // S'assurer que les gestionnaires d'événements sont configurés après l'affichage
        javafx.application.Platform.runLater(() -> {
            controller.setupWindowCloseHandler();
        });
    }

    /**
     * Configure les raccourcis clavier globaux de l'application.
     * 
     * @param scene La scène principale
     * @param controller Le contrôleur principal
     */
    private void setupGlobalKeyboardShortcuts(Scene scene, MainController controller) {
        scene.setOnKeyPressed(event -> {
            if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.S) {
                controller.saveImage();
                event.consume();
            } else if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.O) {
                controller.openImage();
                event.consume();
            } else if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.N) {
                controller.newCanvas();
                event.consume();
            } else if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.W) {
                controller.closeApplication();
                event.consume();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}