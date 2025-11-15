package imageprocessingapp.controller;

// Custom imports
import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.operations.RotateOperation;
import imageprocessingapp.model.operations.SymmetryOperation;
import imageprocessingapp.model.tools.PaintTool;
import imageprocessingapp.model.tools.Tool;
import imageprocessingapp.model.tools.edit.CropTool;
import imageprocessingapp.view.components.ColorDisplay;
import imageprocessingapp.service.DrawingService;
import imageprocessingapp.service.CanvasStateManager;
import imageprocessingapp.service.UnsavedChangesHandler;
import imageprocessingapp.service.FileManagementService;
import imageprocessingapp.service.ImageOperationService;
import imageprocessingapp.service.UndoRedoService;

// JavaFX imports
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
 * 
 * Refactorisé pour utiliser des services dédiés améliorant la testabilité.
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

    @FXML
    private Canvas maskCanvas; // pour l'opacité lors du crop

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
     * Gestionnaire d'état du canvas.
     */
    private CanvasStateManager canvasStateManager;
    
    /**
     * Gestionnaire des modifications non sauvegardées.
     */
    private UnsavedChangesHandler unsavedChangesHandler;
    
    /**
     * Service de gestion des fichiers.
     */
    private FileManagementService fileManagementService;
    
    /**
     * Service des opérations sur l'image.
     */
    private ImageOperationService imageOperationService;
    
    /**
     * Coordinateur des dialogues.
     */
    private DialogCoordinator dialogCoordinator;
    
    /**
     * Gestionnaire des événements.
     */
    private EventHandlerManager eventHandlerManager;
    
    /**
     * Service de gestion undo/redo.
     */
    private UndoRedoService undoRedoService;

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
        return canvasStateManager != null && canvasStateManager.isCanvasModified();
    }

    public boolean isDefaultCanvasModified() {
        return canvasStateManager != null && canvasStateManager.isDefaultCanvasModified();
    }
    
    public boolean hasUnsavedChanges() {
        return canvasStateManager != null && canvasStateManager.hasUnsavedChanges();
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
        setupServices();
        setupDrawingService();
        setupMaskCanvas();
        setupBindings();
        setupEventHandlers();
        setupTools();
        setupColorDisplay();
        setupDelayedInitialization();
        setupZoom();
    }

    /**
     * Initialise tous les services.
     */
    private void setupServices() {
        // Créer le gestionnaire d'état
        canvasStateManager = new CanvasStateManager();
        
        // Créer le gestionnaire des modifications non sauvegardées
        unsavedChangesHandler = new UnsavedChangesHandler(canvasStateManager);
    }
    
    /**
     * Initialise le service undo/redo.
     * Doit être appelé après setupDrawingService() car il dépend de DrawingService.
     */
    private void setupUndoRedoService() {
        if (drawingService != null && imageModel != null) {
            undoRedoService = new UndoRedoService(imageModel, drawingService, currentImage);
        }
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
        drawingService.setOnCanvasModified(() -> {
            canvasStateManager.markAsModified(imageModel.hasImage());
        });
        
        // Initialiser les services qui dépendent de DrawingService
        initializeDependentServices();
        
        // Initialiser le service undo/redo
        setupUndoRedoService();
    }
    
    /**
     * Initialise les services qui dépendent de DrawingService.
     */
    private void initializeDependentServices() {
        // Service de gestion des fichiers
        fileManagementService = new FileManagementService(
                drawingService,
                imageModel,
                canvasStateManager,
                currentImage
        );
        
        // Service des opérations sur l'image
        imageOperationService = new ImageOperationService(
                drawingService,
                imageModel,
                currentImage,
                drawingCanvas,
                maskCanvas,
                canvasStateManager,
                imageContainer
        );
        
        // Coordinateur des dialogues
        dialogCoordinator = new DialogCoordinator(
                this,
                imageView,
                currentImage,
                imageModel
        );
        
        // Gestionnaire des événements
        eventHandlerManager = new EventHandlerManager(
                drawingCanvas,
                imageView,
                activeTool,
                imageModel
        );
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
        imageContainer.getChildren().add(1, drawingCanvas); //le "1" correspond à l'index : ImageView < drawingCanvas < maskCanvas (pour le crop)
    }

    private void setupBindings() {
        imageView.imageProperty().bind(currentImage);
        if (colorDisplay != null) {
            colorDisplay.colorProperty().bind(selectedColor);
        }
    }

    private void setupEventHandlers() {
        if (eventHandlerManager != null) {
            eventHandlerManager.setupMouseHandlers();
        }
        
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
            colorDisplay.setOnColorClick(() -> dialogCoordinator.openColorPicker());
        }
        
        // Configurer ColorDisplay dans ToolSelectorController
        if (toolSelectorController != null) {
            ColorDisplay toolColorDisplay = toolSelectorController.getColorDisplay();
            if (toolColorDisplay != null) {
                toolColorDisplay.setOnColorClick(() -> dialogCoordinator.openColorPicker());
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
     * Configure les raccourcis clavier (Ctrl+S/Cmd+S, Ctrl+Z/Cmd+Z, etc.).
     */
    private void setupKeyboardShortcuts() {
        if (eventHandlerManager != null) {
            // Configurer les callbacks pour les actions
            eventHandlerManager.setOnSaveAction(this::saveImage);
            eventHandlerManager.setOnOpenAction(this::openImage);
            eventHandlerManager.setOnNewAction(this::newCanvas);
            eventHandlerManager.setOnCloseAction(this::closeApplication);
            eventHandlerManager.setOnUndoAction(this::undo);
            eventHandlerManager.setOnRedoAction(this::redo);
            eventHandlerManager.setOnDrawingStart(() -> {
                if (undoRedoService != null) {
                    undoRedoService.saveState();
                }
            });
            eventHandlerManager.setOnCropComplete(this::applyCropping);
            
            // Configurer les raccourcis clavier dans EventHandlerManager
            eventHandlerManager.setupKeyboardShortcuts();
            
            // Ajouter le raccourci pour resetView (non géré par EventHandlerManager)
            // Note: zoomController pourrait ne pas être initialisé encore, mais resetView() vérifie null
            if (imageView != null) {
                Scene scene = imageView.getScene();
                if (scene != null) {
                    scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                        if (event.isShortcutDown() && event.getCode() == javafx.scene.input.KeyCode.R) {
                            resetView();
                            event.consume();
                        }
                    });
                }
            }
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
                if (canvasStateManager.hasUnsavedChanges()) {
                    boolean shouldClose = unsavedChangesHandler.checkForWindowClose(stage);
                    if (!shouldClose) {
                        // L'utilisateur a annulé
                        event.consume();
                    } else {
                        // Si l'utilisateur veut sauvegarder, on sauvegarde
                        // (checkForWindowClose retourne true si l'utilisateur veut sauvegarder ou ignorer)
                        // On ne peut pas savoir ici s'il faut sauvegarder, donc on laisse la fenêtre se fermer
                        // La sauvegarde devrait être gérée avant d'arriver ici
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
        canvasStateManager.markAsModified(imageModel.hasImage());
    }

    // ===== GESTION DES FICHIERS =====
    
    /**
     * Ouvre une image depuis le système de fichiers.
     * Utilise un FileChooser pour sélectionner le fichier.
     */
    public void openImage() {
        Stage ownerStage = getOwnerStage();
        if (ownerStage == null) return;
        
        // Vérifier s'il y a des modifications non sauvegardées
        boolean shouldContinue = unsavedChangesHandler.checkAndHandle(
                "Ouvrir une image",
                "Voulez-vous sauvegarder avant de continuer ?",
                ownerStage
        );
        
        if (!shouldContinue) {
            return; // L'utilisateur a annulé
        }
        
        // Si l'utilisateur veut sauvegarder, on sauvegarde d'abord
        if (canvasStateManager.hasUnsavedChanges()) {
            saveImage();
        }
        
        // Ouvrir l'image
        fileManagementService.openImage(ownerStage);
    }

    /**
     * Sauvegarde l'image actuelle avec les modifications effectuées.
     * Utilise un FileChooser pour sélectionner l'emplacement de sauvegarde.
     */
    public void saveImage() {
        Stage ownerStage = getOwnerStage();
        if (ownerStage != null) {
            fileManagementService.saveImage(ownerStage);
        }
    }

    /**
     * Crée un nouveau canvas de dessin vide.
     * Vérifie les modifications non sauvegardées avant de procéder.
     */
    public void newCanvas() {
        Stage ownerStage = getOwnerStage();
        if (ownerStage == null) return;
        
        // Vérifier s'il y a des modifications non sauvegardées
        boolean shouldContinue = unsavedChangesHandler.checkAndHandle(
                "Créer un nouveau canvas",
                "Voulez-vous sauvegarder avant de créer un nouveau canvas ?",
                ownerStage
        );
        
        if (!shouldContinue) {
            return; // L'utilisateur a annulé
        }
        
        // Si l'utilisateur veut sauvegarder, on sauvegarde d'abord
        if (canvasStateManager.hasUnsavedChanges()) {
            saveImage();
        }
        
        // Créer le nouveau canvas
        fileManagementService.newCanvas(800, 600);
    }

    public void resetView() {
        if (zoomController != null) {
            zoomController.resetView();
        }
    }

    // ===== GESTION DES OUTILS =====
    
    /**
     * Ouvre le sélecteur de couleur.
     * 
     * @param event L'événement du bouton
     */
    public void openColorPicker(ActionEvent event) {
        dialogCoordinator.openColorPicker();
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
        dialogCoordinator.openMosaicDialog();
        }

    // ===== UNDO/REDO =====
    
    /**
     * Annule la dernière opération (undo).
     */
    public void undo() {
        if (undoRedoService != null && undoRedoService.canUndo()) {
            undoRedoService.undo();
            canvasStateManager.markAsModified(imageModel.hasImage());
        }
    }
    
    /**
     * Refait la dernière opération annulée (redo).
     */
    public void redo() {
        if (undoRedoService != null && undoRedoService.canRedo()) {
            undoRedoService.redo();
            canvasStateManager.markAsModified(imageModel.hasImage());
        }
    }
    
    /**
     * Sauvegarde l'état actuel pour undo/redo.
     * Peut être appelé depuis les dialogues pour sauvegarder avant d'appliquer une modification.
     */
    public void saveStateForUndo() {
        if (undoRedoService != null) {
            undoRedoService.saveState();
        }
        }

    // ===== TRANSFORMATIONS =====

    public void applyClockwiseRotation(ActionEvent event) {
        if (undoRedoService != null) {
            undoRedoService.saveState();
        }
        imageOperationService.applyRotation(RotateOperation.Direction.CLOCKWISE);
    }

    public void applyCounterclockwiseRotation(ActionEvent event) {
        if (undoRedoService != null) {
            undoRedoService.saveState();
        }
        imageOperationService.applyRotation(RotateOperation.Direction.COUNTERCLOCKWISE);
    }

    public void applyHorizontalSymmetry(ActionEvent event) {
        if (undoRedoService != null) {
            undoRedoService.saveState();
        }
        imageOperationService.applySymmetry(SymmetryOperation.Axis.HORIZONTAL);
    }

    public void applyVerticalSymmetry(ActionEvent event) {
        if (undoRedoService != null) {
            undoRedoService.saveState();
        }
        imageOperationService.applySymmetry(SymmetryOperation.Axis.VERTICAL);
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

        if (undoRedoService != null) {
            undoRedoService.saveState();
        }

        WritableImage croppedImage = imageOperationService.applyCrop(cropArea);
            if (croppedImage != null) {
            // Désactiver l'outil crop après utilisation
            activeTool.set(null);
        cropTool = null;
        }
    }

    /**
     * Ouvre le dialogue Seam Carving pour redimensionner l'image.
     */
    @FXML
    private void handleSeamCarving() {
        dialogCoordinator.openSeamCarvingDialog();
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
}
