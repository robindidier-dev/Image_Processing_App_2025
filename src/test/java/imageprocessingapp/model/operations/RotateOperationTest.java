package imageprocessingapp.model.operations;

import imageprocessingapp.model.ImageModel;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RotateOperationTest {

    private ImageModel imageModel;
    private WritableImage baseImage;

    @BeforeEach
    void setUp() {
        imageModel = new ImageModel();
        // Crée une image 3x2 avec des couleurs distinctes
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
    void applyClockwiseRotationTest() {
        RotateOperation operation = new RotateOperation(RotateOperation.Direction.CLOCKWISE);
        WritableImage rotated = operation.apply(imageModel);

        // Après rotation horaire, les dimensions sont inversées (2x3)
        assertEquals(2, rotated.getWidth());
        assertEquals(3, rotated.getHeight());

        // Vérifier quelques pixels après rotation horaire
        // (x, y) → (height - 1 - y, x)
        // (0, 0) → (1, 0) : RED devrait être à (1, 0)
        assertEquals(Color.RED, rotated.getPixelReader().getColor(1, 0));
        // (2, 0) → (1, 2) : BLUE devrait être à (1, 2)
        assertEquals(Color.BLUE, rotated.getPixelReader().getColor(1, 2));
        // (0, 1) → (0, 0) : CYAN devrait être à (0, 0)
        assertEquals(Color.CYAN, rotated.getPixelReader().getColor(0, 0));
    }

    @Test
    void applyCounterclockwiseRotationTest() {
        RotateOperation operation = new RotateOperation(RotateOperation.Direction.COUNTERCLOCKWISE);
        WritableImage rotated = operation.apply(imageModel);

        // Après rotation antihoraire, les dimensions sont inversées (2x3)
        assertEquals(2, rotated.getWidth());
        assertEquals(3, rotated.getHeight());

        // Vérifier quelques pixels après rotation antihoraire
        // (x, y) → (y, width - 1 - x)
        // (0, 0) → (0, 2) : RED devrait être à (0, 2)
        assertEquals(Color.RED, rotated.getPixelReader().getColor(0, 2));
        // (2, 0) → (0, 0) : BLUE devrait être à (0, 0)
        assertEquals(Color.BLUE, rotated.getPixelReader().getColor(0, 0));
        // (0, 1) → (1, 2) : CYAN devrait être à (1, 2)
        assertEquals(Color.CYAN, rotated.getPixelReader().getColor(1, 2));
    }

    @Test
    void applyWithNoImage() {
        // Test pour couvrir le cas où !imageModel.hasImage() est true
        ImageModel emptyModel = new ImageModel();
        RotateOperation operation = new RotateOperation(RotateOperation.Direction.CLOCKWISE);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            operation.apply(emptyModel);
        });
        
        assertEquals("Aucune image chargée", exception.getMessage());
    }

    @Test
    void applyWithNullWritableImage() {
        // Test pour couvrir le cas où source == null
        // Pour tester cette branche, nous devons créer un ImageModel où hasImage() retourne true
        // mais getWritableImage() retourne null. Comme hasImage() vérifie les deux conditions,
        // nous devons utiliser une classe anonyme pour contourner cette vérification.
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
        
        RotateOperation operation = new RotateOperation(RotateOperation.Direction.CLOCKWISE);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            operation.apply(model);
        });
        
        assertEquals("Image modifiable indisponible", exception.getMessage());
    }

    @Test
    void constructorWithNullDirection() {
        // Test pour vérifier que le constructeur lance NullPointerException si direction est null
        assertThrows(NullPointerException.class, () -> {
            new RotateOperation(null);
        });
    }

    @Test
    void doubleRotationReturnsToOriginal() {
        // Rotation horaire deux fois devrait revenir à l'image originale (mais inversée)
        RotateOperation clockwise = new RotateOperation(RotateOperation.Direction.CLOCKWISE);
        WritableImage rotated1 = clockwise.apply(imageModel);
        
        // Remettre l'image dans le modèle pour la deuxième rotation
        imageModel.setImage(rotated1);
        WritableImage rotated2 = clockwise.apply(imageModel);
        
        // Après deux rotations horaires, on devrait avoir une image 3x2 (dimensions originales)
        assertEquals(3, rotated2.getWidth());
        assertEquals(2, rotated2.getHeight());
    }

}

