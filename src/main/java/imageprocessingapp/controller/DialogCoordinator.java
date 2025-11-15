package imageprocessingapp.controller;

import imageprocessingapp.model.ImageModel;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Coordonne l'ouverture et la gestion des dialogues modaux.
 * 
 * Extrait de MainController pour améliorer la testabilité et la séparation des responsabilités.
 */
public class DialogCoordinator {
    
    private final MainController mainController;
    private final ImageView imageView;
    private final ObjectProperty<Image> currentImageProperty;
    private final ImageModel imageModel;
    
    /**
     * Constructeur.
     * 
     * @param mainController Le contrôleur principal
     * @param imageView L'ImageView pour obtenir la scène
     * @param currentImageProperty La propriété observable de l'image actuelle
     * @param imageModel Le modèle d'image
     */
    public DialogCoordinator(
            MainController mainController,
            ImageView imageView,
            ObjectProperty<Image> currentImageProperty,
            ImageModel imageModel) {
        this.mainController = mainController;
        this.imageView = imageView;
        this.currentImageProperty = currentImageProperty;
        this.imageModel = imageModel;
    }
    
    /**
     * Ouvre le dialogue de sélection de couleur.
     */
    public void openColorPicker() {
        try {
            Stage ownerStage = getOwnerStage();
            if (ownerStage != null) {
                new ColorPickerDialogController().show(mainController, ownerStage);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le sélecteur de couleur : " + e.getMessage());
        }
    }
    
    /**
     * Ouvre le dialogue de mosaïque.
     */
    public void openMosaicDialog() {
        try {
            Stage ownerStage = getOwnerStage();
            if (ownerStage != null) {
                MosaicDialogController.show(mainController, ownerStage, currentImageProperty, imageModel);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le MosaicDialog : " + e.getMessage());
        }
    }
    
    /**
     * Ouvre le dialogue de Seam Carving.
     */
    public void openSeamCarvingDialog() {
        try {
            Stage ownerStage = getOwnerStage();
            if (ownerStage != null) {
                SeamCarvingDialogController.show(mainController, ownerStage, currentImageProperty, imageModel);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le SeamCarvingDialog : " + e.getMessage());
        }
    }
    
    /**
     * Récupère la fenêtre propriétaire depuis l'ImageView.
     * 
     * @return La Stage propriétaire ou null si non disponible
     */
    private Stage getOwnerStage() {
        if (imageView != null && imageView.getScene() != null) {
            return (Stage) imageView.getScene().getWindow();
        }
        return null;
    }
    
    /**
     * Affiche une alerte avec le titre et le message donnés.
     *
     * @param title   Le titre de l'alerte
     * @param message Le message de l'alerte
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

