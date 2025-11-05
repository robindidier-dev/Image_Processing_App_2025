package imageprocessingapp.model;

import org.junit.jupiter.api.Test;
import javafx.scene.paint.Color;
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
}