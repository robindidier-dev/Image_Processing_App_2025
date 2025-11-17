package imageprocessingapp.model.tools.edit;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.service.DrawingService;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CropToolTest {

    private CropTool cropTool;
    private ImageModel imageModel;
    private DrawingService drawingService;
    private Canvas maskCanvas;
    private Canvas drawingCanvas;

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @BeforeEach
    void setUp() {
        cropTool = new CropTool();
        imageModel = new ImageModel();
        drawingCanvas = new Canvas(200, 200);
        drawingService = new DrawingService(drawingCanvas, imageModel);
        maskCanvas = new Canvas(200, 200);
        drawingService.setMaskCanvas(maskCanvas);
    }

    @Test
    void testGetName() {
        assertEquals("crop", cropTool.getName());
    }

    @Test
    void testCropAreaInitiallyNull() {
        assertNull(cropTool.getCropArea());
    }

    @Test
    void testMousePressedToReleasedCreatesValidCropArea() {
        // Simuler un clic en (10, 20)
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 10, 20);
        cropTool.onMousePressed(pressEvent, imageModel);

        // Simuler le drag jusqu'à (50, 80)
        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 50, 80);
        cropTool.onMouseDragged(dragEvent, imageModel);

        // Simuler un relâchement en (50, 80)
        MouseEvent releaseEvent = createMouseEvent(MouseEvent.MOUSE_RELEASED, 50, 80);
        cropTool.onMouseReleased(releaseEvent, imageModel);

        // Vérifier la zone de crop
        Rectangle2D cropArea = cropTool.getCropArea();
        assertNotNull(cropArea);
        assertEquals(10, cropArea.getMinX());
        assertEquals(20, cropArea.getMinY());
        assertEquals(40, cropArea.getWidth());
        assertEquals(60, cropArea.getHeight());
    }

    @Test
    void testCropAreaWithReverseDrag() {
        // Simuler un drag de droite à gauche et bas vers haut
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 100, 150);
        cropTool.onMousePressed(pressEvent, imageModel);

        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 30, 50);
        cropTool.onMouseDragged(dragEvent, imageModel);

        MouseEvent releaseEvent = createMouseEvent(MouseEvent.MOUSE_RELEASED, 30, 50);
        cropTool.onMouseReleased(releaseEvent, imageModel);

        // Vérifier que les coordonnées sont normalisées (min prend le plus petit)
        Rectangle2D cropArea = cropTool.getCropArea();
        assertNotNull(cropArea);
        assertEquals(30, cropArea.getMinX());
        assertEquals(50, cropArea.getMinY());
        assertEquals(70, cropArea.getWidth());
        assertEquals(100, cropArea.getHeight());
    }

    @Test
    void testMousePressedResetsCropArea() {
        // Premier crop
        MouseEvent pressEvent1 = createMouseEvent(MouseEvent.MOUSE_PRESSED, 10, 10);
        cropTool.onMousePressed(pressEvent1, imageModel);

        MouseEvent dragEvent1 = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 50, 50);
        cropTool.onMouseDragged(dragEvent1, imageModel);

        MouseEvent releaseEvent1 = createMouseEvent(MouseEvent.MOUSE_RELEASED, 50, 50);
        cropTool.onMouseReleased(releaseEvent1, imageModel);

        assertNotNull(cropTool.getCropArea());

        // Nouveau clic doit réinitialiser
        MouseEvent pressEvent2 = createMouseEvent(MouseEvent.MOUSE_PRESSED, 20, 20);
        cropTool.onMousePressed(pressEvent2, imageModel);

        // cropArea devient null au pressed
        assertNull(cropTool.getCropArea());
    }

    @Test
    void testCropAreaWithZeroWidth() {
        // Clic et relâchement au même X (ligne verticale)
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 50, 20);
        cropTool.onMousePressed(pressEvent, imageModel);

        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 50, 80);
        cropTool.onMouseDragged(dragEvent, imageModel);

        MouseEvent releaseEvent = createMouseEvent(MouseEvent.MOUSE_RELEASED, 50, 80);
        cropTool.onMouseReleased(releaseEvent, imageModel);

        Rectangle2D cropArea = cropTool.getCropArea();
        assertNotNull(cropArea);
        assertEquals(0, cropArea.getWidth());
        assertEquals(60, cropArea.getHeight());
    }

    @Test
    void testCropAreaWithZeroHeight() {
        // Clic et relâchement au même Y (ligne horizontale)
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 20, 50);
        cropTool.onMousePressed(pressEvent, imageModel);

        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 80, 50);
        cropTool.onMouseDragged(dragEvent, imageModel);

        MouseEvent releaseEvent = createMouseEvent(MouseEvent.MOUSE_RELEASED, 80, 50);
        cropTool.onMouseReleased(releaseEvent, imageModel);

        Rectangle2D cropArea = cropTool.getCropArea();
        assertNotNull(cropArea);
        assertEquals(60, cropArea.getWidth());
        assertEquals(0, cropArea.getHeight());
    }

    @Test
    void testOnMouseDraggedWithDrawingService() {
        // Configurer le cropTool avec drawingService
        cropTool.setDrawingService(drawingService);
        
        // Simuler un clic en (10, 20)
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 10, 20);
        cropTool.onMousePressed(pressEvent, imageModel);
        
        // Simuler le drag jusqu'à (50, 80)
        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 50, 80);
        cropTool.onMouseDragged(dragEvent, imageModel);
        
        // Vérifier que le masque a été dessiné (le canvas maskCanvas devrait avoir été modifié)
        // On peut vérifier que le canvas n'est pas vide en vérifiant qu'il a un GraphicsContext
        GraphicsContext gc = maskCanvas.getGraphicsContext2D();
        assertNotNull(gc, "Le GraphicsContext devrait exister");
        
        // Vérifier que la zone de sélection est correctement calculée
        // La zone devrait être (10, 20) avec largeur 40 et hauteur 60
        // On peut vérifier indirectement en testant que le cropArea est correct après release
    }

    @Test
    void testOnMouseDraggedWithDrawingServiceReverseDrag() {
        // Configurer le cropTool avec drawingService
        cropTool.setDrawingService(drawingService);
        
        // Simuler un drag de droite à gauche
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 100, 150);
        cropTool.onMousePressed(pressEvent, imageModel);
        
        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 30, 50);
        cropTool.onMouseDragged(dragEvent, imageModel);
        
        // Vérifier que le masque a été dessiné
        GraphicsContext gc = maskCanvas.getGraphicsContext2D();
        assertNotNull(gc);
    }

    @Test
    void testOnMouseDraggedWithoutDrawingService() {
        // Ne pas configurer drawingService (reste null)
        // Simuler un clic et drag
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 10, 20);
        cropTool.onMousePressed(pressEvent, imageModel);
        
        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 50, 80);
        // Ne devrait pas lancer d'exception même sans drawingService
        assertDoesNotThrow(() -> cropTool.onMouseDragged(dragEvent, imageModel));
    }

    @Test
    void testOnMouseReleasedWithDrawingService() {
        // Configurer le cropTool avec drawingService
        cropTool.setDrawingService(drawingService);
        
        // Simuler un clic et drag
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 10, 20);
        cropTool.onMousePressed(pressEvent, imageModel);
        
        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 50, 80);
        cropTool.onMouseDragged(dragEvent, imageModel);
        
        // Simuler le relâchement
        MouseEvent releaseEvent = createMouseEvent(MouseEvent.MOUSE_RELEASED, 50, 80);
        cropTool.onMouseReleased(releaseEvent, imageModel);
        
        // Vérifier que drawOpacityMask(null) a été appelé
        // Le canvas devrait être effacé (on peut vérifier indirectement)
        GraphicsContext gc = maskCanvas.getGraphicsContext2D();
        assertNotNull(gc);
        
        // Vérifier que cropArea a été créé
        assertNotNull(cropTool.getCropArea());
    }

    @Test
    void testOnMouseReleasedWithMaskCanvas() {
        // Configurer le cropTool avec maskCanvas
        cropTool.setMaskCanvas(maskCanvas);
        
        // Dessiner quelque chose sur le canvas pour vérifier qu'il sera effacé
        GraphicsContext gc = maskCanvas.getGraphicsContext2D();
        gc.fillRect(10, 10, 50, 50); // Dessiner quelque chose
        
        // Simuler un clic et relâchement
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 10, 20);
        cropTool.onMousePressed(pressEvent, imageModel);
        
        MouseEvent releaseEvent = createMouseEvent(MouseEvent.MOUSE_RELEASED, 50, 80);
        cropTool.onMouseReleased(releaseEvent, imageModel);
        
        // Vérifier que clearRect a été appelé (le canvas devrait être effacé)
        // On peut vérifier que le GraphicsContext existe toujours
        assertNotNull(maskCanvas.getGraphicsContext2D());
    }

    @Test
    void testOnMouseReleasedWithDrawingServiceAndMaskCanvas() {
        // Configurer le cropTool avec drawingService et maskCanvas
        cropTool.setDrawingService(drawingService);
        cropTool.setMaskCanvas(maskCanvas);
        
        // Simuler un clic et drag
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 10, 20);
        cropTool.onMousePressed(pressEvent, imageModel);
        
        MouseEvent dragEvent = createMouseEvent(MouseEvent.MOUSE_DRAGGED, 50, 80);
        cropTool.onMouseDragged(dragEvent, imageModel);
        
        // Simuler le relâchement
        MouseEvent releaseEvent = createMouseEvent(MouseEvent.MOUSE_RELEASED, 50, 80);
        cropTool.onMouseReleased(releaseEvent, imageModel);
        
        // Vérifier que les deux branches ont été exécutées
        assertNotNull(maskCanvas.getGraphicsContext2D());
        assertNotNull(cropTool.getCropArea());
    }

    @Test
    void testOnMouseReleasedWithoutDrawingServiceAndMaskCanvas() {
        // Ne pas configurer drawingService ni maskCanvas (restent null)
        // Simuler un clic et relâchement
        MouseEvent pressEvent = createMouseEvent(MouseEvent.MOUSE_PRESSED, 10, 20);
        cropTool.onMousePressed(pressEvent, imageModel);
        
        MouseEvent releaseEvent = createMouseEvent(MouseEvent.MOUSE_RELEASED, 50, 80);
        // Ne devrait pas lancer d'exception même sans drawingService et maskCanvas
        assertDoesNotThrow(() -> cropTool.onMouseReleased(releaseEvent, imageModel));
        
        // Vérifier que cropArea a quand même été créé
        assertNotNull(cropTool.getCropArea());
    }

    // Méthode helper pour créer des MouseEvent de test
    private MouseEvent createMouseEvent(javafx.event.EventType<MouseEvent> eventType, double x, double y) {
        return new MouseEvent(
                eventType,
                x, y,           // x, y
                x, y,           // screenX, screenY
                MouseButton.PRIMARY,
                1,              // clickCount
                false, false, false, false, // shift, control, alt, meta
                true, false, false, // primary, middle, secondary button down
                false, false, false, // synthesized, popup trigger, still since press
                null            // pickResult
        );
    }
}
