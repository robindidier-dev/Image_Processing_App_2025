package imageprocessingapp.controller;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.service.edit.SeamCarvingService;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.awt.image.BufferedImage;

/**
 * Contrôleur du dialogue Seam Carving pour redimensionner une image.
 */
public class SeamCarvingDialogController {

    // Boutons du dialog
    @FXML private Button cancelButton;
    @FXML private Button okButton;

    // Sliders pour les dimensions cibles
    @FXML private Slider widthSlider;
    @FXML private Slider heightSlider;

    // Propriété observable pour l'image liée à l'ImageView
    private ObjectProperty<Image> currentImage;

    // Image originale pour la prévisualisation
    private Image originalImage;
    private WritableImage originalWritableImage;

    // Modèle métier de l'image
    private ImageModel imageModel;

    // Stage de la fenêtre dialog
    private Stage dialogStage;

    // Service applicatif pour appliquer le Seam Carving
    private final SeamCarvingService seamCarvingService = new SeamCarvingService();

    // Référence au MainController
    private MainController mainController;

    /**
     * Affiche le dialogue Seam Carving.
     *
     * @param mainController le contrôleur principal
     * @param owner la fenêtre propriétaire
     * @param mainCurrentImage la propriété observable de l'image
     * @param mainImageModel le modèle de l'image
     * @throws Exception si une erreur survient lors du chargement du FXML
     */
    public static void show(MainController mainController, Stage owner, 
                ObjectProperty<Image> mainCurrentImage, ImageModel mainImageModel) throws Exception {

        // Vérification des paramètres d'entrée
        if (mainImageModel == null || !mainImageModel.hasImage()
                || mainCurrentImage == null || mainCurrentImage.get() == null) {
            throw new IllegalArgumentException("Valid currentImage is required to show the SeamCarvingDialog");
        }

        FXMLLoader loader = new FXMLLoader(SeamCarvingDialogController.class.getResource("/imageprocessingapp/dialogs/SeamCarvingDialog.fxml"));
        Parent root = loader.load();

        // Récupérer le controller et initialiser les propriétés
        SeamCarvingDialogController controller = loader.getController();
        controller.setMainController(mainController);
        controller.setCurrentImage(mainCurrentImage);
        controller.setImageModel(mainImageModel);

        // Créer et configurer la fenêtre
        Stage dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        // Position du widget sur la fenêtre
        dialogStage.setX(210);
        dialogStage.setY(95);

        // Modalité : bloque l'interaction avec d'autres éléments
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        controller.setStage(dialogStage);
        dialogStage.setScene(new Scene(root));
        dialogStage.showAndWait();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentImage(ObjectProperty<Image> currentImage) {
        this.currentImage = currentImage;
        this.originalImage = currentImage != null ? currentImage.get() : null;
        if (this.originalImage != null) {
            this.originalWritableImage = imageToWritableImage(this.originalImage);
            // Configurer les sliders une fois que l'image est définie
            if (widthSlider != null && heightSlider != null) {
                setupSliders();
            }
        }
    }

    public void setImageModel(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    public void setStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Initialise les composants FXML et configure les listeners.
     */
    @FXML
    private void initialize() {
        // Gestion des boutons
        cancelButton.setOnAction(event -> cancelPressed());
        okButton.setOnAction(event -> okPressed());

        // Configuration des sliders
        if (widthSlider != null) {
            widthSlider.setId("widthSlider");
        }
        if (heightSlider != null) {
            heightSlider.setId("heightSlider");
        }

        // Ajout de listeners pour la prévisualisation en temps réel
        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview();
        });

        heightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview();
        });

        // Si l'image est déjà définie, configurer les sliders
        if (originalWritableImage != null && widthSlider != null && heightSlider != null) {
            setupSliders();
        }
    }

    /**
     * Configure les valeurs initiales des sliders basées sur l'image actuelle.
     * Cette méthode doit être appelée après que l'image soit définie.
     */
    private void setupSliders() {
        if (originalWritableImage == null) {
            return;
        }

        int currentWidth = (int) originalWritableImage.getWidth();
        int currentHeight = (int) originalWritableImage.getHeight();

        // Définir les valeurs min/max des sliders
        widthSlider.setMin(1);
        widthSlider.setMax(currentWidth);
        widthSlider.setValue(currentWidth);

        heightSlider.setMin(1);
        heightSlider.setMax(currentHeight);
        heightSlider.setValue(currentHeight);
    }

    /**
     * Ferme la fenêtre sans appliquer les modifications.
     */
    private void cancelPressed() {
        if (currentImage != null) {
            currentImage.set(originalImage);
        }
        if (imageModel != null && originalImage != null) {
            imageModel.setImage(originalImage);
        }
        dialogStage.close();
    }

    /**
     * Applique les modifications et ferme la fenêtre.
     */
    private void okPressed() {
        // Mettre à jour la propriété d'image du MainController
        if (mainController != null && currentImage != null) {
            mainController.currentImageProperty().set(currentImage.get());
            imageModel.setImage(currentImage.get());
        }
        dialogStage.close();
    }

    /**
     * Met à jour la prévisualisation de l'image avec les dimensions cibles.
     */
    private void updatePreview() {
        // Vérification des paramètres d'entrée
        if (imageModel == null || !imageModel.hasImage() || currentImage == null || originalWritableImage == null) {
            return;
        }

        int targetWidth = (int) widthSlider.getValue();
        int targetHeight = (int) heightSlider.getValue();

        int currentWidth = (int) originalWritableImage.getWidth();
        int currentHeight = (int) originalWritableImage.getHeight();

        // Vérifier que les dimensions cibles sont valides
        if (targetWidth > currentWidth || targetHeight > currentHeight) {
            // Si les dimensions sont trop grandes, afficher l'image originale
            currentImage.set(originalImage);
            return;
        }

        if (targetWidth == currentWidth && targetHeight == currentHeight) {
            // Si les dimensions sont identiques, afficher l'image originale
            currentImage.set(originalImage);
            return;
        }

        try {
            // Appliquer le Seam Carving avec la boucle de suppression de seams
            WritableImage resizedImage = seamCarvingService.resize(originalWritableImage, targetWidth, targetHeight);
            currentImage.set(resizedImage);
        } catch (IllegalArgumentException e) {
            // En cas d'erreur, afficher l'image originale
            currentImage.set(originalImage);
        }
    }

    /**
     * Convertit une Image JavaFX en WritableImage.
     *
     * @param image l'image à convertir
     * @return une WritableImage
     */
    private WritableImage imageToWritableImage(Image image) {
        if (image == null) {
            return null;
        }

        // Si c'est déjà une WritableImage, la retourner directement
        if (image instanceof WritableImage) {
            return (WritableImage) image;
        }

        // Sinon, convertir via BufferedImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        if (bufferedImage == null) {
            return null;
        }

        WritableImage writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        SwingFXUtils.toFXImage(bufferedImage, writableImage);
        return writableImage;
    }
}
