package imageprocessingapp.model.tools.edit;

import imageprocessingapp.model.ImageModel;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CropToolTest {

    private CropTool cropTool;
    private ImageModel imageModel;

    @BeforeEach
    void setUp() {
        cropTool = new CropTool();
        imageModel = new ImageModel();
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
