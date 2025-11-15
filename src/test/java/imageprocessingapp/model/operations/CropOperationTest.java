package imageprocessingapp.model.operations;

import imageprocessingapp.model.ImageModel;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CropOperationTest {

    private ImageModel imageModel;
    private WritableImage baseImage;


    // On reprend la méthode de test pour SymmetryOperation :
    // on crée une image de test avec des couleurs différentes à chaque position,
    // on applique le crop,
    // et on vérifie que les pixels sont aux bonnes coordonnées.

    @BeforeEach
    void setUp() {
        imageModel = new ImageModel();
        // Crée une image 4x3 avec des couleurs distinctes
        baseImage = new WritableImage(4, 3);

        // Remplir avec des couleurs pour identifier chaque pixel
        baseImage.getPixelWriter().setColor(0, 0, Color.RED);
        baseImage.getPixelWriter().setColor(1, 0, Color.GREEN);
        baseImage.getPixelWriter().setColor(2, 0, Color.BLUE);
        baseImage.getPixelWriter().setColor(3, 0, Color.YELLOW);

        baseImage.getPixelWriter().setColor(0, 1, Color.CYAN);
        baseImage.getPixelWriter().setColor(1, 1, Color.MAGENTA);
        baseImage.getPixelWriter().setColor(2, 1, Color.WHITE);
        baseImage.getPixelWriter().setColor(3, 1, Color.BLACK);

        baseImage.getPixelWriter().setColor(0, 2, Color.PINK);
        baseImage.getPixelWriter().setColor(1, 2, Color.ORANGE);
        baseImage.getPixelWriter().setColor(2, 2, Color.PURPLE);
        baseImage.getPixelWriter().setColor(3, 2, Color.BROWN);

        imageModel.setImage(baseImage);
    }

    @Test
    void applyCropTest() {
        // Crop de (1,1) avec largeur 2 et hauteur 2
        Rectangle2D cropArea = new Rectangle2D(1, 1, 2, 2);
        CropOperation operation = new CropOperation(cropArea);
        WritableImage cropped = operation.apply(imageModel);

        // Vérifie les dimensions
        assertEquals(2, cropped.getWidth());
        assertEquals(2, cropped.getHeight());

        // Vérifie que les pixels correspondent à la zone croppée
        assertEquals(Color.MAGENTA, cropped.getPixelReader().getColor(0, 0));
        assertEquals(Color.WHITE, cropped.getPixelReader().getColor(1, 0));
        assertEquals(Color.ORANGE, cropped.getPixelReader().getColor(0, 1));
        assertEquals(Color.PURPLE, cropped.getPixelReader().getColor(1, 1));
    }


    // Teste un crop qui commence à (0,0) pour vérifier le comportement aux limites
    @Test
    void applyCropFromOriginTest() {
        Rectangle2D cropArea = new Rectangle2D(0, 0, 2, 2);
        CropOperation operation = new CropOperation(cropArea);
        WritableImage cropped = operation.apply(imageModel);

        assertEquals(2, cropped.getWidth());
        assertEquals(2, cropped.getHeight());
        assertEquals(Color.RED, cropped.getPixelReader().getColor(0, 0));
        assertEquals(Color.GREEN, cropped.getPixelReader().getColor(1, 0));
    }


    // Crop toute l'image pour vérifier qu'elle reste identique
    @Test
    void applyCropFullImageTest() {
        Rectangle2D cropArea = new Rectangle2D(0, 0, 4, 3);
        CropOperation operation = new CropOperation(cropArea);
        WritableImage cropped = operation.apply(imageModel);

        assertEquals(4, cropped.getWidth());
        assertEquals(3, cropped.getHeight());
        assertEquals(Color.RED, cropped.getPixelReader().getColor(0, 0));
        assertEquals(Color.BROWN, cropped.getPixelReader().getColor(3, 2));
    }


    // Crop de la plus petite zone possible (1x1 pixel)
    @Test
    void applyCropSinglePixelTest() {
        Rectangle2D cropArea = new Rectangle2D(2, 1, 1, 1);
        CropOperation operation = new CropOperation(cropArea);
        WritableImage cropped = operation.apply(imageModel);

        assertEquals(1, cropped.getWidth());
        assertEquals(1, cropped.getHeight());
        assertEquals(Color.WHITE, cropped.getPixelReader().getColor(0, 0));
    }


}