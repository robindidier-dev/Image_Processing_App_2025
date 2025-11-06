package imageprocessingapp.controller;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.filters.MosaicFilter;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Contrôleur du widget de choix de couleur ColorPicker.
 */

public class MosaicDialogController {

    // Boutons du dialog
    @FXML private Button cancelButton;
    @FXML private Button okButton;

    // Slider du MosaicDialog.fxml qui modifie le nombre de fils du kdtree
    @FXML private Slider spinnerSlider;

    // On récupère l'image chargée ; Propriété observable pour l'image liée à l'ImageView
    private ObjectProperty<Image> currentImage;

    // Modèle métier de l'image permettant d'accéder à l'image et à ses données
    private ImageModel imageModel;

    // Stage de la fenêtre dialog (fenêtre où l'on place l'interface, contient ce que l'utilisateur voit)
    private Stage dialogStage;



     // On passe en argument une Stage pour définir l'owner de la fenêtre

     public static void show(MainController mainController, Stage owner, ObjectProperty<Image> mainCurrentImage, ImageModel mainImageModel) throws Exception {
        FXMLLoader loader = new FXMLLoader(MosaicDialogController.class.getResource("/imageprocessingapp/dialogs/MosaicDialog.fxml"));
        Parent root = loader.load();

        // Ici, on récupère le controller dédié et on lui précise qu'il
        // est 'sous les ordres' du mainController de l'application
        MosaicDialogController controller = loader.getController();
        controller.setMainController(mainController);
        controller.setCurrentImage(mainCurrentImage);
        controller.setImageModel(mainImageModel);


        // On fait apparaître le widget
        Stage dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        // Position du widget sur la fenêtre
        dialogStage.setX(450);
        dialogStage.setY(150);

        // L'apparition de la fenêtre bloque l'interaction avec d'autres éléments de l'application
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        controller.setStage(dialogStage);
        dialogStage.setScene(new Scene(root));
        dialogStage.showAndWait();
    }

    // Le code qui suit permet de communiquer avec MainController
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Setter de la propriété observable image
    public void setCurrentImage(ObjectProperty<Image> CurrentImage) {
        this.currentImage = CurrentImage;
    }

    // Setter du modèle métier image qui va servir lors de l'application de l'effet mosaïque
    public void setImageModel(ImageModel imageModel) {
        this.imageModel = imageModel;
    }


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


        // Ajout d'un listener pour suivre en temps réel la valeur du slider et mettre à jour la prévisualisation à chaque changement.
        spinnerSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Ce code s’exécutera à chaque changement de valeur du slider
            updatePreview(newValue.intValue());
        });
    }

    // Fermeture simple de la fenêtre
    private void cancelPressed() {
        dialogStage.close();
    }

    // Fermeture de la fenêtre et mise à jour
    private void okPressed() {
        // Mettre à jour la propriété d'image du MainController avec la mosaïque actuelle
        if (mainController != null && currentImage != null) {
            mainController.currentImageProperty().set(currentImage.get());
            imageModel.setImage(currentImage.get()); // l'imageModel doit être modifiée aussi, car c'est elle qu'on utilise pour la sauvegarde
        }
        dialogStage.close();
    }

    // Met à jour l'image mosaïque en modifiant la propriété observable
    private void updatePreview(int value) {
        Image mosaicImage = new MosaicFilter(imageModel, value).applyMosaic(); // on utilise l'imageModel plutôt que l'image directement pour pouvoir connaître la taille de l'image, accéder aux pixels, etc.
        currentImage.set(mosaicImage); // met à jour l'image observée, mise à jour automatique de l'ImageView liée (cf .bind() dans MainController)
    }
}
