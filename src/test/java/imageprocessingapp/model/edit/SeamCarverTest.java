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

    @Test
    void findSeamWithRightEdgePath() {
        // Test pour couvrir le cas où le backtracking atteint le bord droit
        // Créer une carte d'énergie où la colonne la plus à droite a toujours la plus faible énergie
        int nbLignes = 5;
        int nbColonnes = 5;
        double[][] cumulativeEnergy = new double[nbLignes][nbColonnes];
        
        // Remplir avec des valeurs élevées partout
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColonnes; j++) {
                cumulativeEnergy[i][j] = 100.0;
            }
        }
        
        // Créer un chemin de faible énergie sur le bord droit (colonne 4)
        // La dernière ligne doit avoir le minimum à la colonne la plus à droite
        for (int i = 0; i < nbLignes; i++) {
            cumulativeEnergy[i][nbColonnes - 1] = 1.0 + i; // Énergie croissante mais toujours minimale
        }
        
        // S'assurer que la colonne avant le bord droit a une énergie plus élevée
        // pour forcer le choix du bord droit pendant le backtracking
        for (int i = 0; i < nbLignes; i++) {
            cumulativeEnergy[i][nbColonnes - 2] = 10.0 + i; // Plus élevé que le bord droit
        }
        
        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);
        
        assertNotNull(seam);
        assertEquals(nbLignes, seam.size());
        
        // Vérifier que la couture passe par le bord droit au moins une fois
        // (ce qui déclenchera la branche else if (j == nbColonnes - 1))
        boolean touchesRightEdge = false;
        for (int colIndex : seam) {
            if (colIndex == nbColonnes - 1) {
                touchesRightEdge = true;
                break;
            }
        }
        
        // Le minimum à la dernière ligne devrait être à la colonne la plus à droite
        // donc le backtracking devrait passer par le bord droit
        assertTrue(touchesRightEdge || seam.get(seam.size() - 1) == nbColonnes - 1,
            "La couture devrait toucher le bord droit pour couvrir cette branche");
    }

    @Test
    void findSeamRightEdgeChoosesRight() {
        // Test pour couvrir la branche : cumulativeEnergy[i][nbColonnes-1] < cumulativeEnergy[i][nbColonnes - 2]
        // → choisit nbColonnes-1
        int nbLignes = 3;
        int nbColonnes = 3;
        double[][] cumulativeEnergy = new double[nbLignes][nbColonnes];
        
        // Remplir avec des valeurs élevées
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColonnes; j++) {
                cumulativeEnergy[i][j] = 100.0;
            }
        }
        
        // Dernière ligne : minimum au bord droit
        cumulativeEnergy[2][2] = 1.0;  // bord droit (minimum)
        cumulativeEnergy[2][1] = 10.0;
        cumulativeEnergy[2][0] = 20.0;
        
        // Ligne 1 : bord droit plus petit que bord-1 (choisit 2)
        cumulativeEnergy[1][2] = 2.0;  // bord droit (plus petit)
        cumulativeEnergy[1][1] = 5.0;  // colonne du milieu
        cumulativeEnergy[1][0] = 50.0;
        
        // Ligne 0 : bord droit plus petit que bord-1 (choisit 2)
        cumulativeEnergy[0][2] = 3.0;  // bord droit (plus petit)
        cumulativeEnergy[0][1] = 6.0;  // colonne du milieu
        cumulativeEnergy[0][0] = 50.0;
        
        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);
        
        assertNotNull(seam);
        assertEquals(nbLignes, seam.size());
        // Le backtracking devrait rester sur le bord droit (colonne 2)
        assertEquals(2, (int) seam.get(0), "Première ligne devrait être au bord droit");
        assertEquals(2, (int) seam.get(1), "Deuxième ligne devrait être au bord droit");
        assertEquals(2, (int) seam.get(2), "Dernière ligne devrait être au bord droit");
    }

    @Test
    void findSeamRightEdgeChoosesLeft() {
        // Test pour couvrir la branche : cumulativeEnergy[i][nbColonnes-1] >= cumulativeEnergy[i][nbColonnes - 2]
        // → choisit nbColonnes-2
        int nbLignes = 3;
        int nbColonnes = 3;
        double[][] cumulativeEnergy = new double[nbLignes][nbColonnes];
        
        // Remplir avec des valeurs élevées
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColonnes; j++) {
                cumulativeEnergy[i][j] = 100.0;
            }
        }
        
        // Dernière ligne : minimum au bord droit
        cumulativeEnergy[2][2] = 1.0;  // bord droit (minimum)
        cumulativeEnergy[2][1] = 10.0;
        cumulativeEnergy[2][0] = 20.0;
        
        // Ligne 1 : bord-1 plus petit que bord droit (choisit 1)
        cumulativeEnergy[1][2] = 5.0;  // bord droit (plus grand)
        cumulativeEnergy[1][1] = 2.0;  // colonne du milieu (plus petite)
        cumulativeEnergy[1][0] = 50.0;
        
        // Ligne 0 : bord-1 plus petit que bord droit (choisit 1)
        cumulativeEnergy[0][2] = 6.0;  // bord droit (plus grand)
        cumulativeEnergy[0][1] = 3.0;  // colonne du milieu (plus petite)
        cumulativeEnergy[0][0] = 50.0;
        
        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);
        
        assertNotNull(seam);
        assertEquals(nbLignes, seam.size());
        // Le backtracking devrait choisir la colonne 1 (nbColonnes-2) quand on est au bord droit
        assertEquals(2, (int) seam.get(2), "Dernière ligne devrait être au bord droit");
        // Les lignes précédentes devraient choisir la colonne 1 car elle a une énergie plus faible
        assertEquals(1, (int) seam.get(1), "Deuxième ligne devrait choisir colonne 1");
        assertEquals(1, (int) seam.get(0), "Première ligne devrait choisir colonne 1");
    }

    @Test
    void findSeamLeftEdgeChoosesLeft() {
        // Test pour couvrir la branche : cumulativeEnergy[i][0] < cumulativeEnergy[i][1]
        // → choisit 0 (bord gauche)
        int nbLignes = 3;
        int nbColonnes = 3;
        double[][] cumulativeEnergy = new double[nbLignes][nbColonnes];
        
        // Remplir avec des valeurs élevées
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColonnes; j++) {
                cumulativeEnergy[i][j] = 100.0;
            }
        }
        
        // Dernière ligne : minimum au bord gauche
        cumulativeEnergy[2][0] = 1.0;  // bord gauche (minimum)
        cumulativeEnergy[2][1] = 10.0;
        cumulativeEnergy[2][2] = 20.0;
        
        // Ligne 1 : bord gauche plus petit que colonne 1 (choisit 0)
        cumulativeEnergy[1][0] = 2.0;  // bord gauche (plus petit)
        cumulativeEnergy[1][1] = 5.0;  // colonne du milieu
        cumulativeEnergy[1][2] = 50.0;
        
        // Ligne 0 : bord gauche plus petit que colonne 1 (choisit 0)
        cumulativeEnergy[0][0] = 3.0;  // bord gauche (plus petit)
        cumulativeEnergy[0][1] = 6.0;  // colonne du milieu
        cumulativeEnergy[0][2] = 50.0;
        
        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);
        
        assertNotNull(seam);
        assertEquals(nbLignes, seam.size());
        // Le backtracking devrait rester sur le bord gauche (colonne 0)
        assertEquals(0, (int) seam.get(0), "Première ligne devrait être au bord gauche");
        assertEquals(0, (int) seam.get(1), "Deuxième ligne devrait être au bord gauche");
        assertEquals(0, (int) seam.get(2), "Dernière ligne devrait être au bord gauche");
    }

    @Test
    void findSeamLeftEdgeChoosesRight() {
        // Test pour couvrir la branche : cumulativeEnergy[i][0] >= cumulativeEnergy[i][1]
        // → choisit 1 (colonne suivante)
        int nbLignes = 3;
        int nbColonnes = 3;
        double[][] cumulativeEnergy = new double[nbLignes][nbColonnes];
        
        // Remplir avec des valeurs élevées
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColonnes; j++) {
                cumulativeEnergy[i][j] = 100.0;
            }
        }
        
        // Dernière ligne : minimum au bord gauche
        cumulativeEnergy[2][0] = 1.0;  // bord gauche (minimum)
        cumulativeEnergy[2][1] = 10.0;
        cumulativeEnergy[2][2] = 20.0;
        
        // Ligne 1 : colonne 1 plus petite que bord gauche (choisit 1)
        cumulativeEnergy[1][0] = 5.0;  // bord gauche (plus grand)
        cumulativeEnergy[1][1] = 2.0;  // colonne du milieu (plus petite)
        cumulativeEnergy[1][2] = 50.0;
        
        // Ligne 0 : colonne 1 plus petite que bord gauche (choisit 1)
        cumulativeEnergy[0][0] = 6.0;  // bord gauche (plus grand)
        cumulativeEnergy[0][1] = 3.0;  // colonne du milieu (plus petite)
        cumulativeEnergy[0][2] = 50.0;
        
        List<Integer> seam = seamCarver.findSeam(cumulativeEnergy);
        
        assertNotNull(seam);
        assertEquals(nbLignes, seam.size());
        // Le backtracking devrait choisir la colonne 1 quand on est au bord gauche
        assertEquals(0, (int) seam.get(2), "Dernière ligne devrait être au bord gauche");
        // Les lignes précédentes devraient choisir la colonne 1 car elle a une énergie plus faible
        assertEquals(1, (int) seam.get(1), "Deuxième ligne devrait choisir colonne 1");
        assertEquals(1, (int) seam.get(0), "Première ligne devrait choisir colonne 1");
    }

}
