package imageprocessingapp.controller;

// Custom imports
import imageprocessingapp.model.tools.EraseTool;
import imageprocessingapp.view.components.ColorDisplay;
import imageprocessingapp.model.tools.PaintTool;
import imageprocessingapp.model.tools.PickerTool;

// JavaFX imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Contrôleur pour la sélection des outils de dessin.
 * 
 * Ce contrôleur gère l'interface de sélection des outils (pinceau, pipette, etc.)
 * et coordonne l'activation des outils avec le MainController.
 * 
 * Pattern Controller : gère les interactions utilisateur pour la sélection d'outils.
 */
public class ToolSelectorController {

    // ===== COMPOSANTS FXML =====
    
    /**
     * Conteneur principal de la barre d'outils.
     * Contient tous les boutons d'outils organisés verticalement.
     */
    @FXML
    private VBox toolbar;
    
    /**
     * ImageView pour l'icône du pinceau.
     * Affiche l'icône du pinceau dans le bouton.
     */
    @FXML
    private ImageView pinceauImageView;
    
    /**
     * ImageView pour l'icône de la pipette.
     * Affiche l'icône de la pipette dans le bouton.
     */
    @FXML
    private ImageView pipetteImageView;

    /**
     * ImageView pour l'icône de la gomme.
     * Affiche l'icône de la gomme dans le bouton.
     */
    @FXML
    private ImageView gommeImageView;

    /**
     * Bouton toggle pour sélectionner l'outil pinceau.
     * Permet d'activer/désactiver l'outil pinceau.
     */
    @FXML
    private ToggleButton pinceauButton;
    
    /**
     * Bouton toggle pour sélectionner l'outil pipette.
     * Permet d'activer/désactiver l'outil pipette.
     */
    @FXML
    private ToggleButton pipetteButton;

    /**
     * Bouton toggle pour sélectionner l'outil gomme.
     * Permet d'activer/désactiver l'outil gomme.
     */
    @FXML
    private ToggleButton gommeButton;

    /**
     * ColorDisplay pour afficher la couleur sélectionnée.
     * Utilisé pour afficher la couleur sélectionnée dans le ColorDisplay.
     */
    @FXML
    private ColorDisplay colorDisplay;

    /**
     * Slider pour choisir la taille de pinceau.
     */
    @FXML
    private Slider brushSizeSlider;

    // ===== RÉFÉRENCES EXTERNES =====
    
    /**
     * Référence vers le contrôleur principal.
     * Utilisée pour communiquer avec le MainController.
     */
    private MainController mainController;
    
    /**
     * Référence vers l'ImageView principale.
     * Utilisée par l'outil pipette pour lire les couleurs.
     */
    private ImageView imageView;
    
    /**
     * Référence vers le Canvas de dessin.
     * Utilisée par l'outil pinceau pour dessiner.
     */
    private Canvas drawingCanvas;

    /**
     * Sélecteur d'outils.
     * Utilisé pour créer le groupe de boutons d'outils.
     */
    private ToggleGroup toolGroup;

    /**
     * Pinceau.
     * Utilisé pour accéder à la méthode setBrushSize().
     */
    private PaintTool paintTool;

    // ===== MÉTHODES DE CONFIGURATION =====
    
    /**
     * Définit la référence vers le contrôleur principal.
     * 
     * @param mainController Le contrôleur principal de l'application
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Définit la référence vers l'ImageView principale.
     * 
     * @param imageView L'ImageView sur laquelle lire les couleurs
     */
    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
    
    /**
     * Définit la référence vers le Canvas de dessin.
     * 
     * @param drawingCanvas Le Canvas sur lequel dessiner
     */
    public void setDrawingCanvas(Canvas drawingCanvas) {
        this.drawingCanvas = drawingCanvas;
    }

    /**
     * Retourne le ColorDisplay.
     * 
     * @return Le ColorDisplay
     */
    public ColorDisplay getColorDisplay() {
        return colorDisplay;
    }

    /**
     * Retourne le Pinceau.
     *
     * @return Le PaintTool
     */
    public void setPaintTool(PaintTool tool) {
        this.paintTool = tool;
    }


    // ===== INITIALISATION =====
    
    /**
     * Méthode d'initialisation appelée automatiquement par JavaFX.
     * Configure les groupes de boutons et les gestionnaires d'événements.
     */
    @FXML
    public void initialize() {
        // Cette méthode est appelée automatiquement après le chargement du FXML dans MainApp.java
        // On crée le binding : l'ImageView suit automatiquement currentImage
        
        // Configurer les gestionnaires d'événements
        setupEventHandlers();

        // Configurer le groupe de boutons d'outils
        setupToggleGroup();
    }
    
    /**
     * Configure les gestionnaires d'événements pour les boutons d'outils.
     */
    private void setupEventHandlers() {
        // Gérer la sélection du pinceau
        pinceauButton.setOnAction(this::pinceauPressed);
        
        // Gérer la sélection de la pipette
        pipetteButton.setOnAction(this::pipettePressed);

        // Gérer la sélection de la gomme
        gommeButton.setOnAction(this::gommePressed);
    }

    /**
     * Configure le listener sur le ToggleGroup pour gérer la désélection des outils.
     */
    private void setupToggleGroup() {
        toolGroup = new ToggleGroup();
        pinceauButton.setToggleGroup(toolGroup);
        pipetteButton.setToggleGroup(toolGroup);
        gommeButton.setToggleGroup(toolGroup);
        
        // Listener pour la désélection
        toolGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && mainController != null) {
                mainController.activeToolProperty().set(null);
            }
        });
    }

    // ===== GESTION DES OUTILS =====
    
    /**
     * Active l'outil pinceau lorsque le bouton est pressé.
     * 
     * @param event L'événement du bouton
     */
    public void pinceauPressed(ActionEvent event) {
        if (pinceauButton.isSelected()) {
            if (mainController != null && drawingCanvas != null) {
                // Créer et configurer l'outil pinceau
                GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
                paintTool = new PaintTool(gc);
                
                // Définir la couleur du pinceau depuis le contrôleur principal
                paintTool.setPaintColor(mainController.selectedColorProperty().get());

                // Equivalent de la méthode brushSizeChanged mais à l'initialisation
                double brushSize = brushSizeSlider.getValue();
                if (this.paintTool != null) {
                    this.paintTool.setBrushSize(brushSize);
                }

                // Configurer le callback de modification
                paintTool.setOnModificationCallback(() -> mainController.markCanvasAsModified());
                
                // Activer l'outil dans le contrôleur principal
                mainController.activeToolProperty().set(paintTool);
            }
        } 
    }

    /**
     * Active l'outil gomme lorsque le bouton est pressé.
     *
     * @param event L'événement du bouton
     */
    public void gommePressed(ActionEvent event) {
        if (gommeButton.isSelected()) {
            if (mainController != null && drawingCanvas != null) {
                // Créer et configurer l'outil pinceau
                GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
                EraseTool eraseTool = new EraseTool(gc);

                // Configurer le callback de modification
                eraseTool.setOnModificationCallback(() -> mainController.markCanvasAsModified());

                // Activer l'outil dans le contrôleur principal
                mainController.activeToolProperty().set(eraseTool);
            }
        }
    }

    /**
     * Active l'outil pipette lorsque le bouton est pressé.
     * 
     * @param event L'événement du bouton
     */
    public void pipettePressed(ActionEvent event) {
        if (pipetteButton.isSelected()) {
            if (mainController != null && imageView != null) {
                // Créer et configurer l'outil pipette
                PickerTool pickerTool = new PickerTool(imageView, drawingCanvas, mainController.selectedColorProperty());
                
                // Activer l'outil dans le contrôleur principal
                mainController.activeToolProperty().set(pickerTool);
            }
        }
    }

    public void brushSizeChanged(MouseEvent mouseEvent) {
        double brushSize = brushSizeSlider.getValue();
        if (this.paintTool != null) {
            this.paintTool.setBrushSize(brushSize);
        }
    }

}
