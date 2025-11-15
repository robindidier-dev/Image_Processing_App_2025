package imageprocessingapp.model.operations;

import imageprocessingapp.model.ImageModel;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SymmetryOperationTest {

    private ImageModel imageModel;
    private WritableImage baseImage;

    @BeforeEach
    void setUp() {
        imageModel = new ImageModel();
        baseImage = new WritableImage(3, 2);

        // Row 0: R, G, B
        baseImage.getPixelWriter().setColor(0, 0, Color.RED);
        baseImage.getPixelWriter().setColor(1, 0, Color.GREEN);
        baseImage.getPixelWriter().setColor(2, 0, Color.BLUE);

        // Row 1: C, M, Y
        baseImage.getPixelWriter().setColor(0, 1, Color.CYAN);
        baseImage.getPixelWriter().setColor(1, 1, Color.MAGENTA);
        baseImage.getPixelWriter().setColor(2, 1, Color.YELLOW);

        imageModel.setImage(baseImage);
    }

    @Test
    void applyVerticalSymmetryTest() {
        SymmetryOperation operation = new SymmetryOperation(SymmetryOperation.Axis.VERTICAL);
        WritableImage flipped = operation.apply(imageModel);

        assertEquals(Color.BLUE, flipped.getPixelReader().getColor(0, 0));
        assertEquals(Color.GREEN, flipped.getPixelReader().getColor(1, 0));
        assertEquals(Color.RED, flipped.getPixelReader().getColor(2, 0));

        assertEquals(Color.YELLOW, flipped.getPixelReader().getColor(0, 1));
        assertEquals(Color.MAGENTA, flipped.getPixelReader().getColor(1, 1));
        assertEquals(Color.CYAN, flipped.getPixelReader().getColor(2, 1));
    }

    @Test
    void applyHorizontalSymmetryTest() {
        SymmetryOperation operation = new SymmetryOperation(SymmetryOperation.Axis.HORIZONTAL);
        WritableImage flipped = operation.apply(imageModel);

        assertEquals(Color.CYAN, flipped.getPixelReader().getColor(0, 0));
        assertEquals(Color.MAGENTA, flipped.getPixelReader().getColor(1, 0));
        assertEquals(Color.YELLOW, flipped.getPixelReader().getColor(2, 0));

        assertEquals(Color.RED, flipped.getPixelReader().getColor(0, 1));
        assertEquals(Color.GREEN, flipped.getPixelReader().getColor(1, 1));
        assertEquals(Color.BLUE, flipped.getPixelReader().getColor(2, 1));
    }

    @Test
    void applyWithNoImage() {
        // Test pour couvrir le cas où !imageModel.hasImage() est true
        ImageModel emptyModel = new ImageModel();
        SymmetryOperation operation = new SymmetryOperation(SymmetryOperation.Axis.VERTICAL);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            operation.apply(emptyModel);
        });
        
        assertEquals("Aucune image chargée", exception.getMessage());
    }

    @Test
    void applyWithNullWritableImage() throws Exception {
        // Test pour couvrir le cas où source == null
        // Pour tester cette branche, nous devons créer un ImageModel où hasImage() retourne true
        // mais getWritableImage() retourne null. Comme hasImage() vérifie les deux conditions,
        // nous devons utiliser la réflexion pour contourner cette vérification.
        ImageModel model = new ImageModel() {
            @Override
            public boolean hasImage() {
                // Retourner true pour passer la première vérification
                return true;
            }
            
            @Override
            public WritableImage getWritableImage() {
                // Retourner null pour déclencher la deuxième vérification
                return null;
            }
        };
        
        SymmetryOperation operation = new SymmetryOperation(SymmetryOperation.Axis.VERTICAL);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            operation.apply(model);
        });
        
        assertEquals("Image modifiable indisponible", exception.getMessage());
    }
}

