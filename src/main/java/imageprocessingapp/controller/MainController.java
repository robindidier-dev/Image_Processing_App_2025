package imageprocessingapp.controller;

// Custom imports
import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.PaintTool;
import imageprocessingapp.model.tools.Tool;
import imageprocessingapp.view.components.ColorDisplay;
import imageprocessingapp.service.DrawingService;

// Java standard imports
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// JavaFX imports
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Contrôleur principal de l'application de traitement d'image.
 * 
 * Ce contrôleur suit le pattern MVC et fait le lien entre :
 * - La View (MainView.fxml) : interface utilisateur
 * - Le Model (ImageModel, Tool, etc.) : logique métier
 * 
 * Il gère les interactions utilisateur et coordonne les différents composants.
 * 
 * Pattern Controller : gère les interactions et coordonne Model ↔ View.
 */
public class MainController {

    // ===== COMPOSANTS FXML =====
    
    @FXML
    private ImageView imageView;
    
    @FXML
    private StackPane imageContainer;
    
    @FXML
    private ColorDisplay colorDisplay;

    @FXML
    private ToolSelectorController toolSelectorController;  

    // ===== PROPRIÉTÉS OBSERVABLES =====
    
    /**
     * Couleur actuellement sélectionnée pour le dessin.
     * Cette propriété est liée aux outils et au ColorDisplay.
     */
    private final ObjectProperty<Color> selectedColor = new SimpleObjectProperty<>(Color.BLACK);
    
    /**
     * Image actuellement chargée dans l'application.
     * Cette propriété est liée à l'ImageView pour l'affichage.
     */
    private final ObjectProperty<Image> currentImage = new SimpleObjectProperty<>();
    
    /**
     * Outil actuellement actif (pinceau, pipette, etc.).
     * Cette propriété détermine le comportement des interactions souris.
     */
    private final ObjectProperty<Tool> activeTool = new SimpleObjectProperty<>();

    // ===== COMPOSANTS INTERNES =====
    
    /**
     * Canvas transparent superposé à l'ImageView pour le dessin.
     * Permet de dessiner par-dessus l'image sans la modifier directement.
     */
    private Canvas drawingCanvas;
    
    /**
     * Modèle de l'image contenant la logique métier.
     * Gère les opérations sur les pixels et les modifications d'image.
     */
    private ImageModel imageModel;

    /**
     * Service de dessin pour gérer les opérations sur le canvas.
     */
    private DrawingService drawingService;
    
    /**
     * Fichier source de l'image actuelle.
     * Utilisé pour la sauvegarde et les opérations de fichier.
     */
    private File sourceFile;

    // ===== PROPRIÉTÉS POUR LE SUIVI DES MODIFICATIONS DES CANVAS =====
    
    /**
     * Indique si le canvas a été modifié depuis la dernière sauvegarde.
     */
    private boolean canvasModified = false;
    
    /**
     * Indique si l'image par défaut (canvas blanc) a été modifiée.
     */
    private boolean defaultCanvasModified = false;

    // ===== GETTERS POUR LES PROPRIÉTÉS =====

    public ObjectProperty<Color> selectedColorProperty() { 
        return selectedColor; 
    }

    public ObjectProperty<Image> currentImageProperty() { 
        return currentImage; 
    }

    public ObjectProperty<Tool> activeToolProperty() { 
        return activeTool; 
    }

    public boolean isCanvasModified() {
        return canvasModified;
    }
    
    public boolean isDefaultCanvasModified() {
        return defaultCanvasModified;
    }
    
    public boolean hasUnsavedChanges() {
        return canvasModified || defaultCanvasModified;
    }

    // ===== INITIALISATION =====
    
    /**
     * Méthode d'initialisation appelée automatiquement par JavaFX.
     * Configure les bindings et initialise les composants.
     */
    @FXML
    public void initialize() {
        // Créer le Canvas transparent pour le dessin
        setupDrawingCanvas();

        // Initialiser le modèle d'image
        imageModel = new ImageModel();

        // Initialiser le service de dessin
        drawingService = new DrawingService(drawingCanvas, imageModel);
        // Configurer le canvas pour le dessin
        drawingService.setupCanvas();
        // Créer un canvas blanc par défaut
        drawingService.createDefaultCanvas();

        // Configurer les bindings
        setupBindings();
        
        // Configurer les gestionnaires d'événements
        setupEventHandlers();
        
        // Configurer les outils
        setupTools();
        
        // Configurer le ColorDisplay
        setupColorDisplay();

        // Configurer les raccourcis clavier et la fermeture de fenêtre
        // Ces méthodes doivent être appelées après que la scène soit disponible
        setupDelayedInitialization();
    }

    private void setupDrawingCanvas() {
        drawingCanvas = new Canvas(800, 600);
        imageContainer.getChildren().add(drawingCanvas);
    }

    private void setupBindings() {
        imageView.imageProperty().bind(currentImage);
        if (colorDisplay != null) {
            colorDisplay.colorProperty().bind(selectedColor);
        }
    }

    private void setupEventHandlers() {
        drawingCanvas.setOnMousePressed(this::handleMousePressed);
        drawingCanvas.setOnMouseDragged(this::handleMouseDragged);
        drawingCanvas.setOnMouseReleased(this::handleMouseReleased);
        
        // Listener pour la couleur du pinceau
        selectedColor.addListener((obs, oldColor, newColor) -> {
            Tool currentTool = activeTool.get();
            if (currentTool instanceof PaintTool) {
                ((PaintTool) currentTool).setPaintColor(newColor);
            }
        });
    }

    private void setupTools() {
        if (toolSelectorController != null) {
            toolSelectorController.setMainController(this);
            toolSelectorController.setImageView(imageView);
            toolSelectorController.setDrawingCanvas(drawingCanvas);
        }
    }

    private void setupColorDisplay() {
        if (colorDisplay != null) {
            colorDisplay.setOnColorClick(() -> openColorPicker(null));
        }
        
        // Configurer ColorDisplay dans ToolSelectorController
        if (toolSelectorController != null) {
            ColorDisplay toolColorDisplay = toolSelectorController.getColorDisplay();
            if (toolColorDisplay != null) {
                toolColorDisplay.setOnColorClick(() -> openColorPicker(null));
                toolColorDisplay.colorProperty().bind(selectedColor);
            }
        }
    }

    /**
     * Configure les éléments qui nécessitent que la scène soit disponible.
     */
    private void setupDelayedInitialization() {
        // Utiliser Platform.runLater pour s'assurer que la scène est disponible
        javafx.application.Platform.runLater(() -> {
            setupKeyboardShortcuts();
            setupWindowCloseHandler();
        });
    }

    // ===== GESTION DES ÉVÉNEMENTS SOURIS ET CLAVIERS =====
    
    /**
     * Gère l'événement de pression de souris.
     * 
     * @param event L'événement de souris
     */
    private void handleMousePressed(MouseEvent event) {
        Tool tool = activeTool.get();

        if (tool != null) {
            tool.onMousePressed(event, imageModel);
        }
    }
    
    /**
     * Gère l'événement de glissement de souris.
     * 
     * @param event L'événement de souris
     */
    private void handleMouseDragged(MouseEvent event) {
        Tool tool = activeTool.get();
        if (tool != null) {
            tool.onMouseDragged(event, imageModel);
        }
    }
    
    /**
     * Gère l'événement de relâchement de souris.
     * 
     * @param event L'événement de souris
     */
    private void handleMouseReleased(MouseEvent event) {
        Tool tool = activeTool.get();
        if (tool != null) {
            tool.onMouseReleased(event, imageModel);
        }
    }

    /**
     * Configure les raccourcis clavier (Ctrl+S/Cmd+S).
     */
    private void setupKeyboardShortcuts() {
        Scene scene = imageView.getScene();
        if (scene != null) {
            scene.setOnKeyPressed(event -> {
                if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.S) {
                    saveImage();
                    event.consume();
                }
            });
        }
    }
    
    /**
     * Configure le gestionnaire de fermeture de fenêtre.
     */
    public void setupWindowCloseHandler() {
        Scene scene = imageView.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.setOnCloseRequest(event -> {
                if (hasUnsavedChanges()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Modifications non sauvegardées");
                    alert.setHeaderText("Vous avez des modifications non sauvegardées.");
                    alert.setContentText("Voulez-vous sauvegarder avant de fermer ?");
                    
                    javafx.scene.control.ButtonType saveButton = new javafx.scene.control.ButtonType("Sauvegarder");
                    javafx.scene.control.ButtonType discardButton = new javafx.scene.control.ButtonType("Ignorer");
                    javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType("Annuler");
                    
                    alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);
                    
                    javafx.scene.control.ButtonType result = alert.showAndWait().orElse(cancelButton);
                    
                    if (result == saveButton) {
                        saveImage();
                        // La fenêtre se fermera automatiquement après la sauvegarde
                    } else if (result == discardButton) {
                        // La fenêtre se fermera automatiquement
                    } else {
                        // Annuler la fermeture
                        event.consume();
                    }
                }
            });
        }
    }

    // ===== MÉTHODES POUR LE SUIVI DES MODIFICATIONS =====
    
    /**
     * Marque le canvas comme modifié.
     */
    public void markCanvasAsModified() {
        if (currentImage.get() == null) {
            // Si aucune image n'est chargée, on modifie le canvas par défaut
            defaultCanvasModified = true;
        } else {
            // Si une image est chargée, on modifie le canvas superposé
            canvasModified = true;
        }
    }
    
    /**
     * Marque le canvas comme non modifié (après sauvegarde).
     */
    private void markCanvasAsSaved() {
        canvasModified = false;
        defaultCanvasModified = false;
    }

    // ===== GESTION DES FICHIERS =====
    
    /**
     * Ouvre une image depuis le système de fichiers.
     * Utilise un FileChooser pour sélectionner le fichier.
     */
    public void openImage() {
        // Vérifier s'il y a des modifications non sauvegardées
        if (hasUnsavedChanges()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Modifications non sauvegardées");
            alert.setHeaderText("Vous avez des modifications non sauvegardées.");
            
            javafx.scene.control.ButtonType saveButton = new javafx.scene.control.ButtonType("Sauvegarder");
            javafx.scene.control.ButtonType discardButton = new javafx.scene.control.ButtonType("Ignorer");
            javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType("Annuler");
            
            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);
            
            javafx.scene.control.ButtonType result = alert.showAndWait().orElse(cancelButton);
            
            if (result == saveButton) {
                saveImage();
                // Continuer avec l'ouverture de la nouvelle image
            } else if (result == discardButton) {
                // Continuer avec l'ouverture de la nouvelle image
            } else {
                // Annuler l'ouverture
                return;
            }
        }
        
        // Setting du FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir une image");
        
        // Définir les extensions acceptées
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Images", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Ouverture du FileChooser et vérification de l'extension
        // Le paramètre null signifie que la boîte de dialogue n'est pas attachée à une fenêtre parente spécifique
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // Vérifier l'extension du fichier
            String fileName = selectedFile.getName().toLowerCase();
            if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                showAlert("Extension invalide", "Veuillez sélectionner un fichier avec l'extension .png, .jpg ou .jpeg.");
                return;
            }
            
            try {
                // Charger l'image
                Image image = new Image(selectedFile.toURI().toString());
                currentImage.set(image);
                
                // Mettre à jour le modèle d'image
                imageModel.setImage(image);
                
                // Redimensionner le Canvas pour correspondre à l'image
                drawingService.resizeCanvasToImage(image);
                
                // Stocker le fichier source pour la sauvegarde
                sourceFile = selectedFile;
                
                // Réinitialiser les flags de modification
                markCanvasAsSaved();
                
            } catch (Exception e) {
                showAlert("Erreur de chargement", "Impossible de charger l'image : " + e.getMessage());
            }
        }
    }

    /**
     * Sauvegarde l'image actuelle avec les modifications effectuées.
     * Utilise un FileChooser pour sélectionner l'emplacement de sauvegarde.
     */
    public void saveImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder l'image");

        // Générer le nom du fichier à enregistrer par défaut
        String defaultFileName = "dessin.png";
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
                // Créer une image composite : image de base + canvas
                Image compositeImage = drawingService.createCompositeImage();
            
                // Convertir JavaFX Image en BufferedImage pour pouvoir l'écrire sur le disque
                // JavaFX Image ne peut pas être sauvegardée directement, il faut passer par BufferedImage
                var bufferedImage = SwingFXUtils.fromFXImage(compositeImage, null);

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
                
                // Marquer comme sauvegardé
                markCanvasAsSaved();
                
            } catch (IOException e) {
                showAlert("Erreur de sauvegarde", "Impossible de sauvegarder l'image : " + e.getMessage());
            }
        }
    }

    // ===== GESTION DES OUTILS =====
    
    /**
     * Ouvre le sélecteur de couleur.
     * 
     * @param event L'événement du bouton
     */
    public void openColorPicker(ActionEvent event) {
        try {
            // Créer et afficher le ColorPickerDialog
            new ColorPickerDialogController().show(this, (javafx.stage.Stage) imageView.getScene().getWindow());
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le sélecteur de couleur : " + e.getMessage());
        }
    }

    // ===== MÉTHODES UTILITAIRES =====
    
    /**
     * Affiche une alerte avec le titre et le message donnés.
     * 
     * @param title Le titre de l'alerte
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