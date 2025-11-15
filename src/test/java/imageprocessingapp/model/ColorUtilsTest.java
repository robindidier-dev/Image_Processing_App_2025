package imageprocessingapp.model;

import org.junit.jupiter.api.Test;
import javafx.scene.paint.Color;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class ColorUtilsTest {

    @Test
    void colorToHex() {
        assertEquals("#000000", ColorUtils.colorToHex(Color.BLACK));
        assertEquals("#ffffff", ColorUtils.colorToHex(Color.WHITE).toLowerCase());
        assertEquals("#ff0000", ColorUtils.colorToHex(Color.RED).toLowerCase());
    }

    @Test
    void hexToColor() {
        assertEquals(Color.web("#00ff00"), ColorUtils.hextoColor("#00ff00"));
        assertEquals(Color.BLACK, ColorUtils.hextoColor("not-a-color"));
    }

    @Test
    void privateConstructorThrowsAssertionError() throws Exception {
        // Test pour couvrir le constructeur privé qui lance une AssertionError
        Constructor<ColorUtils> constructor = ColorUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        // Le constructeur lance une AssertionError, mais elle est wrappée dans InvocationTargetException
        // lors de l'appel via réflexion
        Exception exception = assertThrows(Exception.class, () -> {
            constructor.newInstance();
        });
        
        // Vérifier que la cause est bien une AssertionError
        Throwable cause = exception.getCause();
        assertNotNull(cause, "L'exception devrait avoir une cause");
        assertTrue(cause instanceof AssertionError, 
            "La cause devrait être une AssertionError, mais était: " + cause.getClass());
        assertEquals("ColorUtils: not callable method", cause.getMessage());
    }
}