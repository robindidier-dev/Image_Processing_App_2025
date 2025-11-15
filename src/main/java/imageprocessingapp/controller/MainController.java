package imageprocessingapp.controller;

// Custom imports
import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.operations.SymmetryOperation;
import imageprocessingapp.model.operations.CropOperation;
import imageprocessingapp.model.operations.RotateOperation;
import imageprocessingapp.model.tools.PaintTool;
import imageprocessingapp.model.tools.Tool;
import imageprocessingapp.model.tools.edit.CropTool;
import imageprocessingapp.view.components.ColorDisplay;
import imageprocessingapp.service.DrawingService;
import imageprocessingapp.service.edit.SeamCarvingService;

// Java standard imports
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;

// JavaFX imports
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
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
     * Service de Seam Carving pour gérer les opérations de redimensionnement.
     */
    private SeamCarvingService seamCarvingService;
    
    /**
     * Fichier source de l'image actuelle.
     * Utilisé pour la sauvegarde et les opérations de fichier.
     */
    private File sourceFile;

    /**
     * Instance de l'outil pinceau utilisé pour dessiner.
     * Transmise au contrôleur de sélection d'outils pour modifier ses paramètres.
     */
    private PaintTool paintTool;


    /**
     * Outil pour rogner : utilisation des méthodes onMouseDragged, onMouseReleased, etc -> outil
     */
    private CropTool cropTool;

    /**
     * Contrôleur du zoom
     */
    private ZoomController zoomController;

    // ===== PROPRIÉTÉS POUR LE SUIVI DES MODIFICATIONS DES CANVAS =====
    
    /**
     * Indique si le canvas a été modifié depuis la dernière sauvegarde.
     */
    private boolean canvasModified = false;
    
    /**
     * Indique si l'image par défaut (canvas blanc) a été modifiée.
     */
    private boolean defaultCanvasModified = false;


    @FXML
    private Canvas maskCanvas; // pour l'opacité lors du crop

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
        setupDrawingCanvas();
        setupImageModel();
        setupDrawingService();
        setupMaskCanvas();
        setupBindings();
        setupEventHandlers();
        setupTools();
        setupColorDisplay();
        setupDelayedInitialization();
        setupSeamCarvingService();
        setupZoom();
    }

    private void setupImageModel() {
        imageModel = new ImageModel();
    }

    private void setupDrawingService() {
        drawingService = new DrawingService(drawingCanvas, imageModel);
        drawingService.setupCanvas();
        drawingService.createDefaultCanvas();
        drawingService.setMaskCanvas(maskCanvas);

        // Callback pour les modifications du canvas
        drawingService.setOnCanvasModified(this::markCanvasAsModified);
    }

    private void setupMaskCanvas() {
        if (maskCanvas != null && drawingCanvas != null) {
            // Synchroniser la taille initiale
            maskCanvas.setWidth(drawingCanvas.getWidth());
            maskCanvas.setHeight(drawingCanvas.getHeight());

            // Listeners pour synchronisation automatique
            drawingCanvas.widthProperty().addListener((obs, oldVal, newVal) -> {
                maskCanvas.setWidth(newVal.doubleValue());
            });

            drawingCanvas.heightProperty().addListener((obs, oldVal, newVal) -> {
                maskCanvas.setHeight(newVal.doubleValue());
            });
        }
    }


    private void setupDrawingCanvas() {
        drawingCanvas = new Canvas(800, 600);
        imageContainer.getChildren().add(1,drawingCanvas); //le "1" correspond à l'index : ImageView < drawingCanvas < maskCanvas (pour le crop)
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
            toolSelectorController.setPaintTool(paintTool);
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

    /**
     * Configure le service de Seam Carving.
     */
    private void setupSeamCarvingService() {
        seamCarvingService = new SeamCarvingService();
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

        // Par exemple ici tu peux appeler applyCropping() si l'outil est CropTool
        if (tool instanceof CropTool) {
            applyCropping();
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
                } else if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.O) {
                    openImage();
                    event.consume();
                } else if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.N) {
                    newCanvas();
                    event.consume();
                } else if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.W) {
                    closeApplication();
                    event.consume();
                } else if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.R) {
                    resetView();
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

    public void setupZoom() {
        zoomController = new ZoomController(imageContainer);
        zoomController.setMainController(this);
        zoomController.setupZoom();
        zoomController.setupTranslate();
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
            alert.setContentText("Voulez-vous sauvegarder avant de continuer ?");
            
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

                // Réinitialiser le canvas pour qu'il soit transparent
                drawingService.createDefaultCanvas();
                
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

                // Déterminer le format à partir de l'extension
                String fileName = selectedFile.getName().toLowerCase();
                // On choisit PNG par défaut car c'est un format sans perte
                String format = "png";
                // Les deux extensions pointent vers le même format
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    format = "jpg";
                }

                // Gestion spéciale pour JPEG (pas de transparence)
                BufferedImage bufferedImage;
                if (format.equals("jpg")) {
                    // Pour JPEG, créer une image avec fond blanc si nécessaire
                    BufferedImage originalBuffered = SwingFXUtils.fromFXImage(compositeImage, null);
                    
                    // Créer une nouvelle image RGB (pas d'alpha)
                    bufferedImage = new BufferedImage(
                            originalBuffered.getWidth(),
                            originalBuffered.getHeight(),
                            BufferedImage.TYPE_INT_RGB
                    );
                    
                    Graphics2D g2d = bufferedImage.createGraphics();
                    // Fond blanc seulement si l'image originale avait de la transparence
                    g2d.setColor(java.awt.Color.WHITE);
                    g2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
                    g2d.drawImage(originalBuffered, 0, 0, null);
                    g2d.dispose();
                } else {
                    // Pour PNG, conversion directe (supporte la transparence)
                    bufferedImage = SwingFXUtils.fromFXImage(compositeImage, null);
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

    /**
     * Crée un nouveau canvas de dessin vide.
     * Vérifie les modifications non sauvegardées avant de procéder.
     */
    public void newCanvas() {
        // Vérifier s'il y a des modifications non sauvegardées
        if (hasUnsavedChanges()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Modifications non sauvegardées");
            alert.setHeaderText("Vous avez des modifications non sauvegardées.");
            alert.setContentText("Voulez-vous sauvegarder avant de créer un nouveau canvas ?");
            
            javafx.scene.control.ButtonType saveButton = new javafx.scene.control.ButtonType("Sauvegarder");
            javafx.scene.control.ButtonType discardButton = new javafx.scene.control.ButtonType("Ignorer");
            javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType("Annuler");
            
            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);
            
            javafx.scene.control.ButtonType result = alert.showAndWait().orElse(cancelButton);
            
            if (result == saveButton) {
                saveImage();
                // Continuer avec la création du nouveau canvas
            } else if (result == discardButton) {
                // Continuer avec la création du nouveau canvas
            } else {
                // Annuler la création
                return;
            }
        }
        
        // Créer un nouveau canvas vide
        currentImage.set(null);
        imageModel.clear();
        sourceFile = null;
        
        // Redimensionner le canvas aux dimensions par défaut
        drawingCanvas.setWidth(800);
        drawingCanvas.setHeight(600);
        
        // Créer un canvas blanc par défaut
        drawingService.createDefaultCanvas();
        
        // Réinitialiser les flags de modification
        markCanvasAsSaved();
    }

    public void resetView() {
        zoomController.resetView();
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

    /**
     * Ferme l'application en déclenchant le processus de fermeture standard.
     * Vérifie les modifications non sauvegardées avant de fermer.
     */
    public void closeApplication() {
        Scene scene = imageView.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            // Déclencher l'événement de fermeture de fenêtre
            // Cela activera automatiquement la logique de setupWindowCloseHandler()
            stage.fireEvent(new javafx.stage.WindowEvent(stage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    public void openMosaicDialog() {
        try {
            // Créer et afficher le MosaicDialog
            MosaicDialogController.show(this, (javafx.stage.Stage) imageView.getScene().getWindow(), currentImage, imageModel);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le MosaicDialog : " + e.getMessage());
        }
    }




    // ===== TRANSFORMATIONS =====


    public void applyClockwiseRotation(ActionEvent event) {
        applyRotation(RotateOperation.Direction.CLOCKWISE);
    }

    public void applyCounterclockwiseRotation(ActionEvent event) {
        applyRotation(RotateOperation.Direction.COUNTERCLOCKWISE);
    }

    private void applyRotation(RotateOperation.Direction direction) {
        try {
            // On convertit canvas + image de fond en une image avant de la faire tourner
            WritableImage overlaySnapshot = drawingService.snapshotCanvas();
            WritableImage rotatedOverlay = null;
            if (overlaySnapshot != null) {
                ImageModel overlayModel = new ImageModel(overlaySnapshot);
                rotatedOverlay = new RotateOperation(direction).apply(overlayModel);
            }

            WritableImage rotatedBase = null;
            if (imageModel.hasImage()) {
                rotatedBase = drawingService.applyOperation(new RotateOperation(direction));
                currentImage.set(rotatedBase);
                drawingService.resizeCanvasToImage(rotatedBase);
            }

            drawingService.createDefaultCanvas();
            if (rotatedOverlay != null) {
                drawingService.drawImageOnCanvas(rotatedOverlay);
            }
        } catch (IllegalStateException e) {
            showAlert("Rotation impossible", e.getMessage());
        }
    }

    public void applyHorizontalSymmetry(ActionEvent event) {
        applySymmetry(SymmetryOperation.Axis.HORIZONTAL);
    }

    public void applyVerticalSymmetry(ActionEvent event) {
        applySymmetry(SymmetryOperation.Axis.VERTICAL);
    }

    private void applySymmetry(SymmetryOperation.Axis axis) {
        try {
            // On convertit canvas + image de fond en une image avant de la symétriser
            WritableImage overlaySnapshot = drawingService.snapshotCanvas();
            WritableImage mirroredOverlay = null;
            if (overlaySnapshot != null) {
                ImageModel overlayModel = new ImageModel(overlaySnapshot);
                mirroredOverlay = new SymmetryOperation(axis).apply(overlayModel);
            }

            WritableImage flippedBase = null;
            if (imageModel.hasImage()) {
                flippedBase = drawingService.applyOperation(new SymmetryOperation(axis));
                currentImage.set(flippedBase);
                drawingService.resizeCanvasToImage(flippedBase);
            }

            drawingService.createDefaultCanvas();
            if (mirroredOverlay != null) {
                drawingService.drawImageOnCanvas(mirroredOverlay);
            }
        } catch (IllegalStateException e) {
            showAlert("Symétrie impossible", e.getMessage());
        }
    }

    /**
     * Active l'outil de crop et désélectionne l'outil précédent.
     */
    public void startCropping() {
        // Désélectionner visuellement l'outil actif
        if (toolSelectorController != null) {
            toolSelectorController.deselectAllTools();
        }

        // Créer et configurer l'outil crop
        cropTool = new CropTool();
        cropTool.setImageView(this.imageView);
        cropTool.setDrawingService(drawingService);
        cropTool.setMaskCanvas(maskCanvas);
        activeTool.set(cropTool);
    }

    /**
     * Applique le crop sur l'image composite (fond + dessin).
     */
    public void applyCropping() {
        if (cropTool == null) return;

        Rectangle2D cropArea = cropTool.getCropArea();
        if (cropArea == null) return;

        try {
            // Créer une image composite (fond + canvas)
            WritableImage compositeSnapshot = createCompositeSnapshot();
            if (compositeSnapshot == null) {
                showAlert("Cropping impossible", "Impossible de créer l'image composite.");
                return;
            }

            // Convertir les coordonnées d'affichage vers coordonnées image native
            Rectangle2D scaledCropArea = convertCropAreaToImageCoordinates(cropArea, compositeSnapshot);
            if (scaledCropArea == null) {
                showAlert("Cropping impossible", "Zone de sélection invalide.");
                return;
            }

            // Effectuer le crop
            WritableImage croppedImage = performCrop(compositeSnapshot, scaledCropArea);
            if (croppedImage != null) {
                updateImageAfterCrop(croppedImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Cropping impossible", e.getMessage());
        }
    }

    /**
     * Crée une image composite fusionnant le fond et le canvas.
     *
     * @return L'image composite ou null en cas d'erreur
     */
    private WritableImage createCompositeSnapshot() {
        Image compositeImage = drawingService.createCompositeImage();
        if (compositeImage == null) return null;

        if (compositeImage instanceof WritableImage) {
            return (WritableImage) compositeImage;
        }

        // Convertir en WritableImage si nécessaire
        return new WritableImage(
                compositeImage.getPixelReader(),
                (int) compositeImage.getWidth(),
                (int) compositeImage.getHeight()
        );
    }

    /**
     * Convertit les coordonnées de crop de l'affichage vers l'image native.
     * Applique le facteur d'échelle et clampe aux dimensions de l'image.
     *
     * @param cropArea Zone de crop en coordonnées d'affichage
     * @param image Image native de référence
     * @return Zone de crop en coordonnées image ou null si invalide
     */
    private Rectangle2D convertCropAreaToImageCoordinates(Rectangle2D cropArea, WritableImage image) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double displayWidth = drawingCanvas.getWidth();
        double displayHeight = drawingCanvas.getHeight();

        // Calculer les facteurs d'échelle
        double scaleX = imageWidth / displayWidth;
        double scaleY = imageHeight / displayHeight;

        // Appliquer l'échelle aux coordonnées de crop
        double scaledX = cropArea.getMinX() * scaleX;
        double scaledY = cropArea.getMinY() * scaleY;
        double scaledWidth = cropArea.getWidth() * scaleX;
        double scaledHeight = cropArea.getHeight() * scaleY;

        // Clamper aux dimensions de l'image
        double x = Math.max(0, Math.min(scaledX, imageWidth - 1));
        double y = Math.max(0, Math.min(scaledY, imageHeight - 1));
        double w = Math.min(scaledWidth, imageWidth - x);
        double h = Math.min(scaledHeight, imageHeight - y);

        // Vérifier validité
        if (w <= 0 || h <= 0) return null;

        return new Rectangle2D(x, y, w, h);
    }

    /**
     * Effectue l'opération de crop sur l'image.
     *
     * @param image Image à cropper
     * @param cropArea Zone de crop en coordonnées image
     * @return Image croppée
     */
    private WritableImage performCrop(WritableImage image, Rectangle2D cropArea) {
        ImageModel snapshotModel = new ImageModel(image);
        return new CropOperation(cropArea).apply(snapshotModel);
    }

    /**
     * Met à jour l'interface après le crop : image, canvas et masque.
     * Désactive l'outil crop après utilisation.
     *
     * @param croppedImage Image résultant du crop
     */
    private void updateImageAfterCrop(WritableImage croppedImage) {
        // Mettre à jour l'image affichée
        currentImage.set(croppedImage);
        imageModel.setImage(croppedImage);

        // Redimensionner le canvas de dessin
        drawingService.resizeCanvasToImage(croppedImage);

        // Redimensionner et effacer le maskCanvas
        if (maskCanvas != null) {
            maskCanvas.setWidth(drawingCanvas.getWidth());
            maskCanvas.setHeight(drawingCanvas.getHeight());
            GraphicsContext gc = maskCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, maskCanvas.getWidth(), maskCanvas.getHeight());
        }

        // Réinitialiser le canvas de dessin
        drawingService.createDefaultCanvas();
        markCanvasAsModified();

        // Désactiver l'outil crop
        activeTool.set(null);
        cropTool = null;
    }

    /**
     * Ouvre le dialogue Seam Carving pour redimensionner l'image.
     */
    @FXML
    private void handleSeamCarving() {
        try {
            // Créer et afficher le SeamCarvingDialog
            SeamCarvingDialogController.show(this, (javafx.stage.Stage) imageView.getScene().getWindow(), currentImage, imageModel);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le SeamCarvingDialog : " + e.getMessage());
        }
    }



}
