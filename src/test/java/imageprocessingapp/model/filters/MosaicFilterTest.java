package imageprocessingapp.model.filters;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.structures.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MosaicFilterTest {

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
        int pointCount = 50;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount);
        
        // Vérifier que les dimensions sont correctement initialisées
        // On ne peut pas accéder directement aux champs privés, mais on peut vérifier
        // via les méthodes publiques
        assertNotNull(filter);
    }

    @Test
    void generateRandomPointsCount() {
        int pointCount = 25;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount);
        
        Point2D[] points = filter.generateRandomPoints();
        
        assertNotNull(points);
        assertEquals(pointCount, points.length);
    }

    @Test
    void generateRandomPointsBounds() {
        int pointCount = 30;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount);
        
        Point2D[] points = filter.generateRandomPoints();
        
        for (Point2D point : points) {
            assertTrue(point.x() >= 0 && point.x() < 100, 
                "Point x doit être dans [0, 100[");
            assertTrue(point.y() >= 0 && point.y() < 100, 
                "Point y doit être dans [0, 100[");
        }
    }

    @Test
    void generateRandomPointsIntegers() {
        int pointCount = 20;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount);
        
        Point2D[] points = filter.generateRandomPoints();
        
        for (Point2D point : points) {
            // Les coordonnées doivent être des entiers (même si stockées en double)
            assertEquals((int) point.x(), point.x(), 0.0, 
                "Point x doit être un entier");
            assertEquals((int) point.y(), point.y(), 0.0, 
                "Point y doit être un entier");
        }
    }

    @Test
    void applyMosaicDimensions() {
        int pointCount = 15;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount);
        
        Image result = filter.applyMosaic();
        
        assertNotNull(result);
        assertEquals(100, (int) result.getWidth());
        assertEquals(100, (int) result.getHeight());
    }

    @Test
    void applyMosaicUsesColors() {
        int pointCount = 10;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount);
        
        Image result = filter.applyMosaic();
        var pixelReader = result.getPixelReader();
        
        // Vérifier que l'image résultante n'est pas entièrement noire
        // (ce qui indiquerait un problème)
        boolean hasNonBlackPixel = false;
        for (int x = 0; x < 100; x += 10) {
            for (int y = 0; y < 100; y += 10) {
                Color color = pixelReader.getColor(x, y);
                if (!color.equals(Color.BLACK)) {
                    hasNonBlackPixel = true;
                    break;
                }
            }
            if (hasNonBlackPixel) break;
        }
        
        // Avec une image de test colorée, on devrait avoir des pixels non-noirs
        assertTrue(hasNonBlackPixel, 
            "L'image de mosaïque devrait contenir des couleurs de l'image originale");
    }

    @Test
    void applyMosaicPointCounts() {
        int[] pointCounts = {1, 5, 10, 50, 100};
        
        for (int pointCount : pointCounts) {
            MosaicFilter filter = new MosaicFilter(imageModel, pointCount);
            Image result = filter.applyMosaic();
            
            assertNotNull(result);
            assertEquals(100, (int) result.getWidth());
            assertEquals(100, (int) result.getHeight());
        }
    }

    @Test
    void applyMosaicEffect() {
        // Créer une image simple avec deux zones de couleur distinctes
        WritableImage simpleImage = new WritableImage(50, 50);
        var writer = simpleImage.getPixelWriter();
        
        // Zone rouge à gauche
        for (int x = 0; x < 25; x++) {
            for (int y = 0; y < 50; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        // Zone bleue à droite
        for (int x = 25; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                writer.setColor(x, y, Color.BLUE);
            }
        }
        
        ImageModel simpleModel = new ImageModel(simpleImage);
        MosaicFilter filter = new MosaicFilter(simpleModel, 5);
        
        Image result = filter.applyMosaic();
        assertNotNull(result);
        
        // Vérifier que l'image résultante contient des couleurs de l'original
        var pixelReader = result.getPixelReader();
        boolean hasRed = false;
        boolean hasBlue = false;
        
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                Color color = pixelReader.getColor(x, y);
                // Vérifier si la couleur est proche du rouge ou du bleu
                if (color.getRed() > 0.8 && color.getGreen() < 0.2 && color.getBlue() < 0.2) {
                    hasRed = true;
                }
                if (color.getBlue() > 0.8 && color.getRed() < 0.2 && color.getGreen() < 0.2) {
                    hasBlue = true;
                }
            }
        }
        
        // Au moins une des deux couleurs devrait être présente
        assertTrue(hasRed || hasBlue, 
            "La mosaïque devrait contenir des couleurs de l'image originale");
    }
}

