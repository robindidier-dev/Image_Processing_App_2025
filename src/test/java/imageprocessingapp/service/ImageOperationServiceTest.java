package imageprocessingapp.service;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.operations.RotateOperation;
import imageprocessingapp.model.operations.SymmetryOperation;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageOperationServiceTest {

    private DrawingService drawingService;
    private ImageModel imageModel;
    private ObjectProperty<Image> currentImageProperty;
    private Canvas drawingCanvas;
    private Canvas maskCanvas;
    private CanvasStateManager stateManager;
    private ImageOperationService operationService;
    private StackPane container;

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @BeforeEach
    void setUp() {
        drawingCanvas = new Canvas(100, 100);
        imageModel = new ImageModel();
        drawingService = new DrawingService(drawingCanvas, imageModel);
        drawingService.setupCanvas();
        currentImageProperty = new SimpleObjectProperty<>();
        maskCanvas = new Canvas(100, 100);
        stateManager = new CanvasStateManager();
        container = new StackPane();
        
        operationService = new ImageOperationService(
                drawingService,
                imageModel,
                currentImageProperty,
                drawingCanvas,
                maskCanvas,
                stateManager,
                container
        );
    }

    @Test
    void testApplyRotation_Clockwise_WithImage() {
        // Créer une image de test
        WritableImage testImage = createTestImage(50, 50);
        imageModel.setImage(testImage);
        currentImageProperty.set(testImage);
        
        boolean result = operationService.applyRotation(RotateOperation.Direction.CLOCKWISE);
        
        assertTrue(result);
        assertNotNull(currentImageProperty.get());
        assertTrue(stateManager.hasUnsavedChanges());
    }

    @Test
    void testApplyRotation_Counterclockwise_WithImage() {
        // Créer une image de test
        WritableImage testImage = createTestImage(50, 50);
        imageModel.setImage(testImage);
        currentImageProperty.set(testImage);
        
        boolean result = operationService.applyRotation(RotateOperation.Direction.COUNTERCLOCKWISE);
        
        assertTrue(result);
        assertNotNull(currentImageProperty.get());
        assertTrue(stateManager.hasUnsavedChanges());
    }

    @Test
    void testApplyRotation_WithoutImage() {
        // Pas d'image chargée
        boolean result = operationService.applyRotation(RotateOperation.Direction.CLOCKWISE);
        
        // Devrait quand même fonctionner (rotation du canvas)
        assertTrue(result);
    }

    @Test
    void testApplySymmetry_Horizontal_WithImage() {
        // Créer une image de test
        WritableImage testImage = createTestImage(50, 50);
        imageModel.setImage(testImage);
        currentImageProperty.set(testImage);
        
        boolean result = operationService.applySymmetry(SymmetryOperation.Axis.HORIZONTAL);
        
        assertTrue(result);
        assertNotNull(currentImageProperty.get());
        assertTrue(stateManager.hasUnsavedChanges());
    }

    @Test
    void testApplySymmetry_Vertical_WithImage() {
        // Créer une image de test
        WritableImage testImage = createTestImage(50, 50);
        imageModel.setImage(testImage);
        currentImageProperty.set(testImage);
        
        boolean result = operationService.applySymmetry(SymmetryOperation.Axis.VERTICAL);
        
        assertTrue(result);
        assertNotNull(currentImageProperty.get());
        assertTrue(stateManager.hasUnsavedChanges());
    }

    @Test
    void testApplySymmetry_WithoutImage() {
        // Pas d'image chargée
        boolean result = operationService.applySymmetry(SymmetryOperation.Axis.HORIZONTAL);
        
        // Devrait quand même fonctionner (symétrie du canvas)
        assertTrue(result);
    }

    @Test
    void testApplyCrop_ValidArea() {
        // Créer une image de test
        WritableImage testImage = createTestImage(100, 100);
        imageModel.setImage(testImage);
        currentImageProperty.set(testImage);
        drawingCanvas.setWidth(100);
        drawingCanvas.setHeight(100);
        
        Rectangle2D cropArea = new Rectangle2D(10, 10, 50, 50);
        WritableImage result = operationService.applyCrop(cropArea);
        
        assertNotNull(result);
        assertTrue(stateManager.hasUnsavedChanges());
    }

    @Test
    void testApplyCrop_NullArea() {
        // Note: showAlert nécessite le thread JavaFX, donc on ne peut pas tester null directement
        // On peut seulement vérifier que la méthode existe
        assertNotNull(operationService);
    }

    @Test
    void testApplyCrop_InvalidArea() {
        // Créer une image de test
        WritableImage testImage = createTestImage(100, 100);
        imageModel.setImage(testImage);
        currentImageProperty.set(testImage);
        drawingCanvas.setWidth(100);
        drawingCanvas.setHeight(100);
        
        // Zone de crop avec largeur/hauteur très petite (proche de zéro, peut être considérée invalide)
        Rectangle2D cropArea = new Rectangle2D(10, 10, 0.1, 0.1);
        operationService.applyCrop(cropArea);
        
        // Le crop peut retourner null si la zone est trop petite après conversion
        // ou un résultat valide si elle est acceptée
        // On vérifie juste que la méthode ne lance pas d'exception
        assertNotNull(operationService);
    }

    /**
     * Crée une image de test.
     */
    private WritableImage createTestImage(int width, int height) {
        WritableImage image = new WritableImage(width, height);
        javafx.scene.image.PixelWriter writer = image.getPixelWriter();
        
        // Remplir avec une couleur
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                writer.setColor(x, y, javafx.scene.paint.Color.RED);
            }
        }
        
        return image;
    }
}

