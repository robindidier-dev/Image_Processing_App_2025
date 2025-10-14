package imageprocessingapp.controller;

import imageprocessingapp.view.components.ColorDisplay;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Contrôleur du widget de choix de couleur ColorPicker.
 */

public class ColorPickerDialogController {

    // Import de tous les objets contenus dans le widget
    @FXML private ColorPicker colorPicker;
    @FXML private ColorDisplay colorDisplay;
    @FXML private Button cancelButton;
    @FXML private Button okButton;



    // Le code qui suit permet de communiquer avec MainController
    // En effet, on doit lier les propriétés (couleur choisie, etc.).
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;

        // On initialise tout avec la couleur en cours d'utilisation
        // Les deux lignes qui suivent ne peuvent pas être placées dans initialize()
        // car initialize() est appelé avant setMainController
        Color currentColor = mainController.selectedColorProperty().get();
        colorPicker.setValue(currentColor);
        // On lie la couleur en train d'être choisie dans le Picker à ColorDisplay
        colorDisplay.colorProperty().bind(colorPicker.valueProperty());
    }


    // Le code qui suit permet de récupérer la Stage créée dans le modèle (ColorPickerDialog.java)
    // et donc de pouvoir notamment lier l'appui d'un bouton à la fermeture de la fenêtre
    private Stage dialogStage;

    public void setStage(Stage stage) {
        this.dialogStage = stage;
    }

    // On utilise des composants FXML : pas de constructeur dans cette classe
    // On a à la place une méthode initialize à définir.

    @FXML
    private void initialize() {
        // Gestion des boutons
        cancelButton.setOnAction(event -> cancelPressed());
        okButton.setOnAction(event -> okPressed());
    }

    // Fermeture simple de la fenêtre
    private void cancelPressed() {
        dialogStage.close();
    }

    // Fermeture de la fenêtre et mise à jour de la couleur selectionnée
    private void okPressed() {
        // Mise à jour de la couleur sélectionnée
        mainController.selectedColorProperty().set(colorPicker.getValue());
        dialogStage.close();
    }
}
