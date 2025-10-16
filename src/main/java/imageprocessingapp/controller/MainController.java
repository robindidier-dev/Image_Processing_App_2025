package imageprocessingapp.controller;

// Java standard imports
import imageprocessingapp.model.tools.PickerTool;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// JavaFX imports
import imageprocessingapp.model.tools.PaintTool;
import imageprocessingapp.model.tools.ToolSelector;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

// Application imports
import imageprocessingapp.model.tools.Tool;

/**
 * Contrôleur principal de l'application de traitement d'image.
 * 
 * Ce contrôleur suit le pattern MVC et fait le lien entre :
 * - La View (MainView.fxml) : interface utilisateur
 * - Le Model (ImageModel, Tool, etc.) : logique métier
 * 
 * Il gère les interactions utilisateur et coordonne les différents composants.
 */

public class MainController {

    @FXML // Annotation pour indiquer que la variable est liée à un composant FXML
    // À la lecture du FXML dans MainApp.java, imageView est automatiquement initialisé
    private ImageView imageView; // id identique à celui dans le fxml (pour le binding)

    // Le mot-clé final est utilisé ici pour :
    // Garantir que les références ne peuvent pas être réassignées à un autre ObjectProperty
    // PS: les valeurs des références peuvent toujours être modifiées via les méthodes de l'ObjectProperty
    private final ObjectProperty<Color> selectedColor = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Image> currentImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Tool> activeTool = new SimpleObjectProperty<>();

    // Getters publics pour les propriétés observables
    public ObjectProperty<Color> selectedColorProperty() { return selectedColor; }
    public ObjectProperty<Image> currentImageProperty() { return currentImage; }
    public ObjectProperty<Tool> activeToolProperty() { return activeTool; }

    private File sourceFile;


    // L'import de la VBox du MainView.fxml est aussi nécessaire pour pouvoir initialiser les boutons pinceau, pipette etc.
    @FXML
    private VBox toolbar;


    //De même, on importe les ImageView des outils pour pouvoir associer ces images avec les ToggleButtons correspondants
    @FXML
    private ImageView pinceauImageView;

    @FXML
    private ImageView pipetteImageView;


    @FXML
    private ToggleButton pinceauButton;

    @FXML
    private ToggleButton pipetteButton;



    @FXML
    public void initialize() {
        // Cette méthode est appelée automatiquement après le chargement du FXML dans MainApp.java
        // On crée le binding : l'ImageView suit automatiquement currentImage
        imageView.imageProperty().bind(currentImage);


        ToolSelector toolSelector = new ToolSelector(toolbar);

        // On appelle la méthode setupToggleGroup pour associer chaque bouton au même ToggleGroup,
        // assurant que seuls un des boutons peut être sélectionné à la fois (pas de sélection multiple des outils)
        toolSelector.setupToggleGroup(pinceauButton);
        toolSelector.setupToggleGroup(pipetteButton);

    }

    public void openImage() {
        // Setting du FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        // Définir les extensions acceptées
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Images", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Ouverture du FileChooser et vérification de l'extension
        // Le paramètre null signifie que la boîte de dialogue n'est pas attachée à une fenêtre parente spécifique
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String fileName = selectedFile.getName().toLowerCase();
            if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                showAlert("Invalid Extension", "Please select a file with .png, .jpg or .jpeg extension.");
                return;
            }
            Image image = new Image(selectedFile.toURI().toString());
            currentImage.set(image);
            // Stocker le fichier source pour la sauvegarde
            sourceFile = selectedFile;
        }
    }

    public void saveImage() {
        // Vérifier qu'une image est chargée
        if (currentImage.get() == null) {
            showAlert("No Image", "Please load an image before saving.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");

        // Générer le nom par du fichier à enregistrer
        String defaultFileName = "image_edited.png";
        if (sourceFile != null) {
            String sourceName = sourceFile.getName();
            // Séparer le nom et l'extension
            int dotIndex = sourceName.lastIndexOf('.');
            if (dotIndex > 0) {
                String nameWithoutExt = sourceName.substring(0, dotIndex);
                String extension = sourceName.substring(dotIndex); // inclut le point
                defaultFileName = nameWithoutExt + "_edited" + extension;
            }
        }
        fileChooser.setInitialFileName(defaultFileName);

        // Définir le répertoire où enregistrer (même répertoire que le fichier source)
        if (sourceFile != null && sourceFile.getParentFile() != null) {
            fileChooser.setInitialDirectory(sourceFile.getParentFile());
        }

        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            try {
                // Convertir JavaFX Image en BufferedImage pour pouvoir l'écrire sur le disque
                // JavaFX Image ne peut pas être sauvegardée directement, il faut passer par BufferedImage
                var bufferedImage = SwingFXUtils.fromFXImage(currentImage.get(), null);

                // Déterminer le format à partir de l'extension
                String fileName = selectedFile.getName().toLowerCase();
                // On choisit PNG par défaut car c'est un format sans perte
                String format = "png";
                // Les deux extensions pointent vers le même format
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    format = "jpg";
                }

                // Sauvegarder l'image
                ImageIO.write(bufferedImage, format, selectedFile);
            } catch (IOException e) {
                showAlert("Save Error", "Failed to save image: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    //Méthode pour activer l'outil pinceau lorsque le bouton est pressé
    public void onPinceauSelected(ActionEvent event) {
        System.out.println("pinceau sélectionné");
        activeTool.set(new PaintTool(new Canvas()));
        /// A FAIRE : remplacer new Canvas par un canvas transparent à placer au dessus de l'image
    }

    //Méthode pour activer l'outil pipette lorsque le bouton est pressé
    public void onPipetteSelected(ActionEvent event) {
        System.out.println("pipette sélectionnée");

        //le constructeur prend l'imageView sur laquelle lire la couleur, et la variable selectedColor qu'on va mettre à jour
        activeTool.set(new PickerTool(imageView, selectedColor));
    }
}