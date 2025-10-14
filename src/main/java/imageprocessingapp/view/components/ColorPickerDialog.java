package imageprocessingapp.view.components;

import imageprocessingapp.controller.ColorPickerDialogController;
import imageprocessingapp.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Point d'entrée du widget de sélection de couleur.
 * Modèle MVC :
 *  Cette classe (ColorPickerDialog) est le modèle est initialise le widget.
 *  Le controller est ColorPickerDialogController ;
 *  La view est ColorPickerDialog.fxml.
 */

public class ColorPickerDialog {

    // On passe en argument une Stage pour définir l'owner de la fenêtre

    public void show(MainController mainController, Stage owner) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/imageprocessingapp/dialogs/ColorPickerDialog.fxml"));
        Parent root = loader.load();

        // Ici, on récupère le controller dédié et on lui précise qu'il
        // est 'sous les ordres' du mainController de l'application
        ColorPickerDialogController controller = loader.getController();
        controller.setMainController(mainController);

        // On fait apparaître le widget
        Stage dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        // Position du widget sur la fenêtre
        dialogStage.setX(300);
        dialogStage.setY(200);

        // L'apparition de la fenêtre bloque l'interaction avec d'autres éléments de l'application
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        controller.setStage(dialogStage);
        dialogStage.setScene(new Scene(root));
        dialogStage.showAndWait();
    }
}










