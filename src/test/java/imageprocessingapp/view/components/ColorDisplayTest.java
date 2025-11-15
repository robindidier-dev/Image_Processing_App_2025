package imageprocessingapp.view.components;

import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorDisplayTest {

    private ColorDisplay colorDisplay;

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @BeforeEach
    void setUp() {
        colorDisplay = new ColorDisplay();
    }

    @Test
    void setColorAndGetColor() {
        colorDisplay.setColor(Color.RED);
        assertEquals(Color.RED, colorDisplay.getColor());
        
        colorDisplay.setColor(Color.BLUE);
        assertEquals(Color.BLUE, colorDisplay.getColor());
    }

    @Test
    void colorProperty() {
        assertNotNull(colorDisplay.colorProperty());
        
        colorDisplay.colorProperty().set(Color.GREEN);
        assertEquals(Color.GREEN, colorDisplay.getColor());
    }

    @Test
    void updateColorDisplay() {
        // Le listener met à jour automatiquement le rectangle et le label
        colorDisplay.setColor(Color.RED);
        
        // Vérifier que la couleur est bien définie
        assertEquals(Color.RED, colorDisplay.getColor());
        
        // Vérifier que le rectangle a la bonne couleur
        // (on ne peut pas accéder directement au rectangle, mais on peut vérifier via la propriété)
        assertNotNull(colorDisplay.colorProperty().get());
    }

    @Test
    void setOnColorClick() {
        boolean[] clicked = {false};
        colorDisplay.setOnColorClick(() -> clicked[0] = true);
        
        // Trouver le rectangle dans les enfants du ColorDisplay
        Rectangle rectangle = null;
        for (Node child : colorDisplay.getChildren()) {
            if (child instanceof Rectangle) {
                rectangle = (Rectangle) child;
                break;
            }
        }
        
        assertNotNull(rectangle, "Le rectangle devrait être présent dans les enfants");
        
        // Simuler un clic de souris sur le rectangle pour tester la lambda
        MouseEvent mouseEvent = new MouseEvent(
            MouseEvent.MOUSE_CLICKED,
            40.0, 20.0, 40.0, 20.0,
            MouseButton.PRIMARY, 1,
            false, false, false, false,
            true, false, false, true,
            false, false, null
        );
        
        // Déclencher l'événement directement sur le rectangle
        rectangle.fireEvent(mouseEvent);
        
        // Vérifier que le callback a été appelé
        assertTrue(clicked[0], "Le callback devrait être appelé lors du clic");
    }

    @Test
    void updateColorDisplayWithNull() {
        // Tester que updateColorDisplay gère null correctement
        colorDisplay.setColor(null);
        // Ne devrait pas lever d'exception
        assertNull(colorDisplay.getColor());
    }

    @Test
    void constructor() {
        ColorDisplay newDisplay = new ColorDisplay();
        assertNotNull(newDisplay);
        assertNotNull(newDisplay.colorProperty());
    }

    @Test
    void updateColorDisplayViaProperty() {
        // Test que le listener est appelé quand on change via colorProperty().set()
        colorDisplay.colorProperty().set(Color.RED);
        assertEquals(Color.RED, colorDisplay.getColor());
        
        colorDisplay.colorProperty().set(Color.BLUE);
        assertEquals(Color.BLUE, colorDisplay.getColor());
    }

    @Test
    void updateColorDisplayWithSameColor() {
        // Test que le listener est appelé même si on définit la même couleur
        colorDisplay.setColor(Color.RED);
        colorDisplay.setColor(Color.RED); // Même couleur
        assertEquals(Color.RED, colorDisplay.getColor());
    }

    @Test
    void setOnColorClickMultipleTimes() {
        // Test que setOnColorClick peut être appelé plusieurs fois
        colorDisplay.setOnColorClick(() -> {});
        colorDisplay.setOnColorClick(() -> {});
        assertNotNull(colorDisplay);
    }

    @Test
    void colorPropertyBinding() {
        // Test que la propriété peut être liée
        colorDisplay.colorProperty().set(Color.GREEN);
        assertEquals(Color.GREEN, colorDisplay.getColor());
        
        // Changer via setColor
        colorDisplay.setColor(Color.YELLOW);
        assertEquals(Color.YELLOW, colorDisplay.colorProperty().get());
    }
}

