package imageprocessingapp.model.structures;

import imageprocessingapp.model.ImageModel;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnergyCalculatorTest {

    private ImageModel imageModel;
    private WritableImage testImage;

    @BeforeEach
    void setUp() {
        // Créer une image de test 100x100 avec des couleurs variées
        testImage = new WritableImage(100, 100);
        var pixelWriter = testImage.getPixelWriter();

        // Remplir l'image avec un motif de couleurs
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                Color color = Color.rgb(
                        (x * 255) / 100,
                        (y * 255) / 100,
                        128
                );
                pixelWriter.setColor(x, y, color);
            }
        }
        imageModel = new ImageModel(testImage);
    }

    @Test
    void constructorWorks() {
        EnergyCalculator energyCalculator = new EnergyCalculator();
        assertNotNull(energyCalculator);
    }

    // Teste la nullité de l'énergie sur une image uniforme
    @Test
    void uniformImageEnergyZero() {
        WritableImage img = new WritableImage(10, 10);
        PixelWriter pw = img.getPixelWriter();
        for (int y=0; y<10; y++)
            for (int x=0; x<10; x++)
                pw.setColor(x, y, Color.BLACK);

        ImageModel model = new ImageModel(img);
        EnergyCalculator ec = new EnergyCalculator();
        double[][] energy = ec.computeEnergyMap(model);

        for (int y=0; y<10; y++)
            for (int x=0; x<10; x++)
                assertEquals(0.0, energy[y][x], 1e-9);
    }

    @Test
    void computeEnergyMapBasicTest() {
        EnergyCalculator ec = new EnergyCalculator();
        double[][] energy = ec.computeEnergyMap(imageModel);

        int width = (int) testImage.getWidth();
        int height = (int) testImage.getHeight();

        // Vérifier la taille de la matrice
        assertEquals(height, energy.length);
        assertEquals(width, energy[0].length);

        // Vérifier qu'au moins un pixel a une énergie positive
        boolean hasPositive = false;
        for (int y = 0; y < height && !hasPositive; y++) {
            for (int x = 0; x < width; x++) {
                if (energy[y][x] > 1e-6) {       // test positivité sur des flottants
                    hasPositive = true;
                    break;
                }
            }
        }
        assertTrue(hasPositive, "La carte d'énergie devrait contenir des valeurs positives.");
    }
}

