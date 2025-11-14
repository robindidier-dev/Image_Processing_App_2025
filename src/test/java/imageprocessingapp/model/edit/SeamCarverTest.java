package imageprocessingapp.model.edit;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class SeamCarverTest {

    private WritableImage testImage;
    private SeamCarver seamCarver;

    @BeforeEach
    void setUp() {
        // Créer une image de test 10x10 avec des couleurs variées
        testImage = new WritableImage(10, 10);
        var pixelWriter = testImage.getPixelWriter();

        // Remplir l'image avec un dégradé de couleurs
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                Color color = Color.rgb(
                        (x * 255) / 10,
                        (y * 255) / 10,
                        128
                );
                pixelWriter.setColor(x, y, color);
            }
        }

        seamCarver = new SeamCarver();
    }

    @Test
    void computeCumulativeEnergyDimensions() {
        double[][] energyMap = new double[10][10];

        // Remplir avec des valeurs d'énergie de test
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                energyMap[i][j] = Math.random() * 100;
            }
        }

        double[][] cumulativeEnergy = seamCarver.computeCumulativeEnergy(energyMap);

        assertNotNull(cumulativeEnergy);
        assertEquals(10, cumulativeEnergy.length, "La hauteur doit être identique");
        assertEquals(10, cumulativeEnergy[0].length, "La largeur doit être identique");
    }

    @Test
    void computeCumulativeEnergyFirstRow() {
        double[][] energyMap = new double[5][5];

        // Remplir avec des valeurs connues
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                energyMap[i][j] = i + j;
            }
        }

        double[][] cumulativeEnergy = seamCarver.computeCumulativeEnergy(energyMap);

        // La première ligne doit être identique à l'énergie originale
        for (int j = 0; j < 5; j++) {
            assertEquals(energyMap[0][j], cumulativeEnergy[0][j], 0.001,
                    "La première ligne doit copier l'énergie originale");
        }
    }

    @Test
    void computeCumulativeEnergyIncreasing() {
        double[][] energyMap = new double[10][10];

        // Remplir avec des valeurs constantes positives
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                energyMap[i][j] = 10.0;
            }
        }

        double[][] cumulativeEnergy = seamCarver.computeCumulativeEnergy(energyMap);

        // L'énergie cumulative doit augmenter ligne par ligne
        for (int j = 0; j < 10; j++) {
            for (int i = 1; i < 10; i++) {
                assertTrue(cumulativeEnergy[i][j] > cumulativeEnergy[i-1][j],
                        "L'énergie cumulative doit augmenter vers le bas");
            }
        }
    }

    @Test
    void findSeamReturnsValidLength() {
        double[][] cumulativeEnergy = new double[10][10];

        // Remplir avec des valeurs aléatoires
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                cumulativeEnergy[i][j] = Math.random() * 100;
            }
        }

        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);

        assertNotNull(seam);
        assertEquals(10, seam.size(), "La couture doit avoir un indice par ligne");
    }

    @Test
    void findSeamIndicesInBounds() {
        double[][] cumulativeEnergy = new double[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                cumulativeEnergy[i][j] = Math.random() * 100;
            }
        }

        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);

        for (int i = 0; i < seam.size(); i++) {
            int colIndex = seam.get(i);
            assertTrue(colIndex >= 0 && colIndex < 10,
                    "L'indice de colonne doit être dans [0, 10[");
        }
    }

    @Test
    void findSeamConsecutiveIndicesDifferByAtMostOne() {
        double[][] cumulativeEnergy = new double[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                cumulativeEnergy[i][j] = Math.random() * 100;
            }
        }

        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);

        // Vérifier que les indices consécutifs ne diffèrent pas de plus de 1
        for (int i = 1; i < seam.size(); i++) {
            int diff = Math.abs(seam.get(i) - seam.get(i - 1));
            assertTrue(diff <= 1,
                    "Les indices consécutifs doivent différer d'au plus 1");
        }
    }

    @Test
    void findSeamChoosesMinimumPath() {
        // Créer une carte d'énergie avec un chemin clairement minimal
        double[][] cumulativeEnergy = new double[5][5];

        // Remplir avec des valeurs élevées
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                cumulativeEnergy[i][j] = 100.0;
            }
        }

        // Créer un chemin de faible énergie au centre (colonne 2)
        for (int i = 0; i < 5; i++) {
            cumulativeEnergy[i][2] = 1.0;
        }

        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);

        // La couture devrait suivre la colonne 2 (ou proche)
        // car c'est le chemin de moindre énergie
        boolean followsLowEnergyPath = true;
        for (int i = 0; i < seam.size(); i++) {
            // Les indices devraient être 2 ou très proches
            if (Math.abs(seam.get(i) - 2) > 1) {
                followsLowEnergyPath = false;
            }
        }
        assertTrue(followsLowEnergyPath,
                "La couture devrait suivre le chemin de moindre énergie");
    }

    @Test
    void removeSeamReducesWidth() {
        List<Integer> seam = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            seam.add(5); // Supprimer la colonne 5 pour chaque ligne
        }

        WritableImage result = seamCarver.removeSeam(testImage, seam);

        assertNotNull(result);
        assertEquals(9, (int) result.getWidth(),
                "La largeur doit être réduite de 1");
        assertEquals(10, (int) result.getHeight(),
                "La hauteur doit rester identique");
    }

    @Test
    void removeSeamPreservesPixels() {
        // Créer une image simple avec des couleurs distinctes
        WritableImage simpleImage = new WritableImage(5, 3);
        var writer = simpleImage.getPixelWriter();

        // Remplir avec des couleurs uniques par colonne
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.PINK};
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 3; y++) {
                writer.setColor(x, y, colors[x]);
            }
        }

        // Supprimer la colonne 2 (BLUE)
        List<Integer> seam = List.of(2, 2, 2);

        WritableImage result = seamCarver.removeSeam(simpleImage, seam);
        var reader = result.getPixelReader();

        // Vérifier que les pixels restants sont corrects
        assertEquals(4, (int) result.getWidth());

        // Colonne 0 et 1 doivent rester identiques
        for (int y = 0; y < 3; y++) {
            assertEquals(Color.RED, reader.getColor(0, y), "Colonne 0 doit être RED");
            assertEquals(Color.GREEN, reader.getColor(1, y), "Colonne 1 doit être GREEN");
        }

        // Colonnes 3 et 4 décalées à 2 et 3
        for (int y = 0; y < 3; y++) {
            assertEquals(Color.YELLOW, reader.getColor(2, y), "Colonne 2 doit être YELLOW (ancienne 3)");
            assertEquals(Color.PINK, reader.getColor(3, y), "Colonne 3 doit être PINK (ancienne 4)");
        }
    }

    @Test
    void removeSeamEdgeCase() {
        // Supprimer le bord gauche
        List<Integer> seamLeft = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            seamLeft.add(0);
        }

        WritableImage resultLeft = seamCarver.removeSeam(testImage, seamLeft);
        assertEquals(9, (int) resultLeft.getWidth());

        // Supprimer le bord droit
        List<Integer> seamRight = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            seamRight.add(9);
        }

        WritableImage resultRight = seamCarver.removeSeam(testImage, seamRight);
        assertEquals(9, (int) resultRight.getWidth());
    }

}
