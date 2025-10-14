package imageprocessingapp;

import imageprocessingapp.view.components.ColorPickerDialog;
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/imageprocessingapp/view/MainView.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Image Processing App");
        primaryStage.show();


        // (PROVISOIRE) Ouvre une fenêtre de choix de la couleur
        // (PROVISOIRE) au démarrage de l'application
        ColorPickerDialog dialogStage = new ColorPickerDialog();
        dialogStage.show(controller, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}