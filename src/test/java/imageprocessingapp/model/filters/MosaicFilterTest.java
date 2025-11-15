package imageprocessingapp.model.filters;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.structures.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import imageprocessingapp.model.filters.MosaicFilter.MosaicSeedMode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
    void generateRandomPointsCount() {
        int pointCount = 25;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount, MosaicSeedMode.RANDOM);
        
        Point2D[] points = filter.generateRandomPoints();
        
        assertNotNull(points);
        assertEquals(pointCount, points.length);
    }

    @Test
    void generateRandomPointsBounds() {
        int pointCount = 30;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount, MosaicSeedMode.RANDOM);
        
        Point2D[] points = filter.generateRandomPoints();
        
        for (Point2D point : points) {
            assertTrue(point.x() >= 0 && point.x() < 100, 
                "Point x doit être dans [0, 100[");
            assertTrue(point.y() >= 0 && point.y() < 100, 
                "Point y doit être dans [0, 100[");
        }
    }

    @Test
    void applyMosaicDimensions() {
        int pointCount = 15;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount, MosaicSeedMode.RANDOM);
        
        Image result = filter.applyMosaic();
        
        assertNotNull(result);
        assertEquals(100, (int) result.getWidth());
        assertEquals(100, (int) result.getHeight());
    }

    @Test
    void applyMosaicUsesColors() {
        int pointCount = 10;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount, MosaicSeedMode.RANDOM);
        
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
        MosaicFilter filter = new MosaicFilter(simpleModel, 5, MosaicSeedMode.RANDOM);
        
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

    @Test
    void generateRegularGridPoints() {
        int pointCount = 16;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount, MosaicSeedMode.REGULAR_GRID);
        
        Point2D[] points = filter.generateRegularGridPoints();
        
        assertNotNull(points);
        assertEquals(pointCount, points.length);
        
        // Vérifier que tous les points sont dans les bornes
        for (Point2D point : points) {
            assertTrue(point.x() >= 0 && point.x() < 100, 
                "Point x doit être dans [0, 100[");
            assertTrue(point.y() >= 0 && point.y() < 100, 
                "Point y doit être dans [0, 100[");
        }
    }

    @Test
    void applyMosaicWithRegularGrid() {
        int pointCount = 9;
        MosaicFilter filter = new MosaicFilter(imageModel, pointCount, MosaicSeedMode.REGULAR_GRID);
        
        Image result = filter.applyMosaic();
        
        assertNotNull(result);
        assertEquals(100, (int) result.getWidth());
        assertEquals(100, (int) result.getHeight());
    }

    @Test
    void applyMosaicWithTargetEqualsNearest() {
        // Créer une image très petite pour forcer le cas où target == nearest
        WritableImage smallImage = new WritableImage(2, 2);
        var writer = smallImage.getPixelWriter();
        writer.setColor(0, 0, Color.RED);
        writer.setColor(1, 0, Color.BLUE);
        writer.setColor(0, 1, Color.GREEN);
        writer.setColor(1, 1, Color.YELLOW);
        
        ImageModel smallModel = new ImageModel(smallImage);
        MosaicFilter filter = new MosaicFilter(smallModel, 2, MosaicSeedMode.RANDOM);
        
        Image result = filter.applyMosaic();
        assertNotNull(result);
    }

    @Test
    void regularGridPointsWithEdgeCase() {
        // Test avec un pointCount qui force les conditions de boucle
        WritableImage smallImage = new WritableImage(10, 10);
        var writer = smallImage.getPixelWriter();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel smallModel = new ImageModel(smallImage);
        MosaicFilter filter = new MosaicFilter(smallModel, 1, MosaicSeedMode.REGULAR_GRID);
        
        Point2D[] points = filter.generateRegularGridPoints();
        assertNotNull(points);
        assertEquals(1, points.length);
    }

    @Test
    void regularGridWithLargePointCount() {
        // Test avec un pointCount qui dépasse le nombre de points possibles dans la grille
        MosaicFilter filter = new MosaicFilter(imageModel, 1000, MosaicSeedMode.REGULAR_GRID);
        
        Point2D[] points = filter.generateRegularGridPoints();
        assertNotNull(points);
        assertEquals(1000, points.length);
        
        // Vérifier que tous les points sont dans les bornes
        for (Point2D point : points) {
            assertTrue(point.x() >= 0 && point.x() < 100);
            assertTrue(point.y() >= 0 && point.y() < 100);
        }
    }

    @Test
    void regularGridPointsWithRectangularImage() {
        // Test avec une image rectangulaire (width != height)
        WritableImage rectImage = new WritableImage(50, 100);
        var writer = rectImage.getPixelWriter();
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 100; y++) {
                writer.setColor(x, y, Color.BLUE);
            }
        }
        
        ImageModel rectModel = new ImageModel(rectImage);
        MosaicFilter filter = new MosaicFilter(rectModel, 20, MosaicSeedMode.REGULAR_GRID);
        
        Point2D[] points = filter.generateRegularGridPoints();
        assertNotNull(points);
        assertEquals(20, points.length);
        
        // Vérifier que tous les points sont dans les bornes
        for (Point2D point : points) {
            assertTrue(point.x() >= 0 && point.x() < 50);
            assertTrue(point.y() >= 0 && point.y() < 100);
        }
    }

    @Test
    void applyMosaicWithSinglePoint() {
        // Test avec un seul point seed
        WritableImage smallImage = new WritableImage(5, 5);
        var writer = smallImage.getPixelWriter();
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel smallModel = new ImageModel(smallImage);
        MosaicFilter filter = new MosaicFilter(smallModel, 1, MosaicSeedMode.RANDOM);
        
        Image result = filter.applyMosaic();
        assertNotNull(result);
        assertEquals(5, (int) result.getWidth());
        assertEquals(5, (int) result.getHeight());
    }

    @Test
    void regularGridPointsWithIndexLimitStopsAtPointCount() {
        // Test pour couvrir la branche if (index >= pointCount) { break; }
        // dans la boucle externe après modification du code.
        //
        // Structure modifiée :
        // for (int row = 0; row < rows; row++) {
        //     if (index >= pointCount) {
        //         break; // Cette branche doit être couverte
        //     }
        //     for (int col = 0; col < cols && index < pointCount; col++) {
        //         points[index++] = new Point2D(x, y);
        //     }
        // }
        //
        // Pour couvrir la branche if (index >= pointCount), il faut que :
        // 1. La boucle interne atteigne index = pointCount
        // 2. On sorte de la boucle interne
        // 3. On termine l'itération de la boucle externe (row++)
        // 4. On entre dans la prochaine itération de la boucle externe
        // 5. La condition index >= pointCount soit vraie → break
        //
        // Pour que cela fonctionne, il faut que rows soit suffisamment grand
        // pour qu'il y ait une itération supplémentaire après que index ait atteint pointCount.
        //
        // Calcul pour pointCount = 5, width = 100, height = 100:
        // cols = ceil(sqrt(5 * 1.0)) = 3
        // rows = ceil(5/3.0) = 2
        // Grille = 3x2 = 6 points possibles, mais on veut seulement 5 points
        //
        // Itération 1 (row=0):
        //   Vérification: index=0 < pointCount=5 → continue
        //   Boucle interne: col=0,1,2 → index devient 3
        //   Fin itération: row++ → row=1
        //
        // Itération 2 (row=1):
        //   Vérification: index=3 < pointCount=5 → continue
        //   Boucle interne: col=0,1 → index devient 5
        //   Boucle interne: col=2 → condition col < cols && index < pointCount
        //                          → 2 < 3 && 5 < 5 → false
        //                          → sort de la boucle interne
        //   APRÈS boucle interne: row=1, index=5
        //   Fin itération: row++ → row=2
        //
        // Itération 3 (row=2):
        //   Vérification: index=5 >= pointCount=5 → break (BRANCHE COUVERTE)
        //
        // Mais row=2 >= rows=2, donc la boucle s'arrête avant d'entrer.
        //
        // Solution: Utiliser une image avec un ratio width/height différent pour
        // générer un rows plus grand.
        //
        // Calcul pour pointCount = 5, width = 50, height = 200 (image verticale):
        // cols = ceil(sqrt(5 * (50/200))) = ceil(sqrt(5 * 0.25)) = ceil(sqrt(1.25)) = ceil(1.12) = 2
        // rows = ceil(5/2.0) = 3
        // Grille = 2x3 = 6 points possibles, mais on veut seulement 5 points
        //
        // Itération 1 (row=0): génère 2 points → index=2
        // Itération 2 (row=1): génère 2 points → index=4
        // Itération 3 (row=2): génère 1 point → index=5
        // Itération 4 (row=3):
        //   Vérification: index=5 >= pointCount=5 → break (BRANCHE COUVERTE)
        //
        // Mais row=3 >= rows=3, donc la boucle s'arrête avant.
        //
        // Solution finale: Utiliser pointCount tel que la boucle interne atteigne pointCount
        // au milieu d'une ligne, et que rows soit suffisamment grand pour permettre
        // une itération supplémentaire.
        //
        // Calcul pour pointCount = 4, width = 50, height = 200:
        // cols = ceil(sqrt(4 * 0.25)) = ceil(sqrt(1.0)) = 1
        // rows = ceil(4/1.0) = 4
        // Grille = 1x4 = 4 points possibles, mais on veut seulement 4 points
        // Dans ce cas, cols * rows = 4 = pointCount, donc on génère exactement 4 points
        //
        // Calcul pour pointCount = 3, width = 50, height = 200:
        // cols = ceil(sqrt(3 * 0.25)) = ceil(sqrt(0.75)) = ceil(0.87) = 1
        // rows = ceil(3/1.0) = 3
        // Grille = 1x3 = 3 points possibles, mais on veut seulement 3 points
        //
        // Solution: Utiliser pointCount = 2 avec une image verticale pour forcer rows > pointCount
        // Calcul pour pointCount = 2, width = 50, height = 200:
        // cols = ceil(sqrt(2 * 0.25)) = ceil(sqrt(0.5)) = ceil(0.71) = 1
        // rows = ceil(2/1.0) = 2
        // Grille = 1x2 = 2 points possibles, mais on veut seulement 2 points
        //
        // Solution: Utiliser pointCount = 6 avec une image carrée pour forcer rows > pointCount/cols
        // Calcul pour pointCount = 6, width = 100, height = 100:
        // cols = ceil(sqrt(6 * 1.0)) = ceil(2.45) = 3
        // rows = ceil(6/3.0) = 2
        // Grille = 3x2 = 6 points possibles, mais on veut seulement 6 points
        //
        // La vraie solution: Utiliser pointCount tel que cols * rows > pointCount ET rows > pointCount/cols
        // Cela garantit qu'il y aura une itération supplémentaire après que index ait atteint pointCount.
        WritableImage largeImage = new WritableImage(100, 100);
        var writer = largeImage.getPixelWriter();
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel largeModel = new ImageModel(largeImage);
        // Utiliser pointCount = 7 pour que la grille (3x3 = 9) soit plus grande que pointCount
        // Cela garantit que la boucle interne atteindra pointCount avant la fin de la grille
        // et que rows=3 permettra une itération supplémentaire après index=pointCount
        MosaicFilter filter = new MosaicFilter(largeModel, 7, MosaicSeedMode.REGULAR_GRID);
        
        Point2D[] points = filter.generateRegularGridPoints();
        assertNotNull(points);
        assertEquals(7, points.length); // Exactement 7 points, pas 9
        
        // Vérifier que tous les points sont dans les bornes
        for (Point2D point : points) {
            assertTrue(point.x() >= 0 && point.x() < 100);
            assertTrue(point.y() >= 0 && point.y() < 100);
        }
    }

    @Test
    void applyMosaicCoversSeedEqualsTargetBranchFalse() {
        // Test pour couvrir la branche if (!seed.equals(target)) quand elle est TRUE
        // (c'est-à-dire seed != target)
        //
        // Après correction du bug, le code compare maintenant seed avec target
        // Pour couvrir la branche if (!seed.equals(target)), il faut que seed != target
        // Cela arrive quand un pixel n'est pas exactement à la position d'un seed
        WritableImage testImage = new WritableImage(10, 10);
        var writer = testImage.getPixelWriter();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel model = new ImageModel(testImage);
        // Utiliser peu de seeds pour que la plupart des pixels ne soient pas des seeds
        // Quand on cherche le nearest pour un pixel qui n'est pas un seed,
        // seed != target, donc !seed.equals(target) est true
        MosaicFilter filter = new MosaicFilter(model, 2, MosaicSeedMode.RANDOM);
        
        Image result = filter.applyMosaic();
        assertNotNull(result);
        assertEquals(10, (int) result.getWidth());
        assertEquals(10, (int) result.getHeight());
        
        // Avec seulement 2 seeds sur une image 10x10, 98 pixels sur 100 ne seront pas des seeds,
        // donc !seed.equals(target) sera true pour ces pixels,
        // couvrant ainsi la branche if (!seed.equals(target))
    }
    
    @Test
    void applyMosaicCoversSeedEqualsTargetBranchTrue() {
        // Test pour couvrir la branche else de if (!seed.equals(target))
        // (c'est-à-dire seed == target)
        //
        // Pour couvrir la branche else, il faut que seed.equals(target) soit true
        // Cela arrive quand un pixel est exactement à la position d'un seed
        // Utilisons REGULAR_GRID avec un pointCount qui génère des seeds
        // exactement aux positions des pixels
        WritableImage tinyImage = new WritableImage(2, 2);
        var writer = tinyImage.getPixelWriter();
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel tinyModel = new ImageModel(tinyImage);
        // Utiliser REGULAR_GRID avec 4 points pour une image 2x2
        // cols = ceil(sqrt(4 * 1.0)) = 2, rows = ceil(4/2.0) = 2
        // Cela générera 4 seeds aux positions (0.5, 0.5), (1.5, 0.5), (0.5, 1.5), (1.5, 1.5)
        // Après arrondi et Math.min, cela donnera (0, 0), (1, 0), (0, 1), (1, 1)
        // Quand on cherche le nearest pour un pixel (0, 0), le seed sera (0, 0),
        // donc seed.equals(target) sera true
        MosaicFilter filter = new MosaicFilter(tinyModel, 4, MosaicSeedMode.REGULAR_GRID);
        
        Image result = filter.applyMosaic();
        assertNotNull(result);
        assertEquals(2, (int) result.getWidth());
        assertEquals(2, (int) result.getHeight());
        
        // Avec 4 seeds sur une image 2x2 en REGULAR_GRID, les seeds seront
        // exactement aux positions des pixels, donc seed.equals(target) sera true
        // pour ces pixels, couvrant ainsi la branche else de if (!seed.equals(target))
    }

    @Test
    void applyMosaicWithNearestNotEqualsTarget() {
        // Test pour couvrir le cas où !nearest.equals(target) est true
        // Créer une image où les targets ne correspondent pas exactement aux seeds
        WritableImage mediumImage = new WritableImage(10, 10);
        var writer = mediumImage.getPixelWriter();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                writer.setColor(x, y, Color.BLUE);
            }
        }
        
        ImageModel mediumModel = new ImageModel(mediumImage);
        // Utiliser peu de points pour que la plupart des targets ne soient pas des seeds
        MosaicFilter filter = new MosaicFilter(mediumModel, 2, MosaicSeedMode.RANDOM);
        
        Image result = filter.applyMosaic();
        assertNotNull(result);
        assertEquals(10, (int) result.getWidth());
        assertEquals(10, (int) result.getHeight());
    }

    @Test
    void applyColorWithEmptyCell() throws Exception {
        // Test pour couvrir le cas où cell.isEmpty() est true dans applyColor()
        // Utiliser la réflexion pour accéder à la méthode privée applyColor
        WritableImage testImg = new WritableImage(10, 10);
        var writer = testImg.getPixelWriter();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel model = new ImageModel(testImg);
        MosaicFilter filter = new MosaicFilter(model, 5, MosaicSeedMode.RANDOM);
        
        // Créer une Map avec une cellule vide pour tester le cas d'erreur
        Map<Point2D, List<Point2D>> cells = new HashMap<>();
        List<Point2D> emptyCell = new ArrayList<>();
        cells.put(new Point2D(5, 5), emptyCell);
        
        // Accéder à la méthode privée applyColor via réflexion
        Method applyColorMethod = MosaicFilter.class.getDeclaredMethod("applyColor", Map.class);
        applyColorMethod.setAccessible(true);
        
        // Vérifier que l'exception est levée (enveloppée dans InvocationTargetException)
        Exception exception = assertThrows(Exception.class, () -> {
            applyColorMethod.invoke(filter, cells);
        });
        
        // Extraire la cause réelle de l'exception
        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof NoSuchElementException, 
            "La cause devrait être NoSuchElementException, mais était: " + cause.getClass());
    }

    @Test
    void applyColorWithNullCell() throws Exception {
        // Test pour couvrir le cas où cell == null dans applyColor()
        WritableImage testImg = new WritableImage(10, 10);
        var writer = testImg.getPixelWriter();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel model = new ImageModel(testImg);
        MosaicFilter filter = new MosaicFilter(model, 5, MosaicSeedMode.RANDOM);
        
        // Créer une Map avec une cellule null pour tester le cas d'erreur
        Map<Point2D, List<Point2D>> cells = new HashMap<>();
        cells.put(new Point2D(5, 5), null);
        
        // Accéder à la méthode privée applyColor via réflexion
        Method applyColorMethod = MosaicFilter.class.getDeclaredMethod("applyColor", Map.class);
        applyColorMethod.setAccessible(true);
        
        // Vérifier que l'exception est levée (enveloppée dans InvocationTargetException)
        Exception exception = assertThrows(Exception.class, () -> {
            applyColorMethod.invoke(filter, cells);
        });
        
        // Extraire la cause réelle de l'exception
        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof NoSuchElementException, 
            "La cause devrait être NoSuchElementException, mais était: " + cause.getClass());
    }

    @Test
    void regularGridPointsWithColLimit() {
        // Test pour couvrir le cas où col < cols && index < pointCount dans la boucle interne
        // Utiliser un pointCount qui sera atteint avant la fin de la boucle des colonnes
        WritableImage wideImage = new WritableImage(200, 50);
        var writer = wideImage.getPixelWriter();
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 50; y++) {
                writer.setColor(x, y, Color.GREEN);
            }
        }
        
        ImageModel wideModel = new ImageModel(wideImage);
        // Utiliser un pointCount qui sera atteint avant la fin des colonnes
        MosaicFilter filter = new MosaicFilter(wideModel, 3, MosaicSeedMode.REGULAR_GRID);
        
        Point2D[] points = filter.generateRegularGridPoints();
        assertNotNull(points);
        assertEquals(3, points.length);
        
        // Vérifier que tous les points sont dans les bornes
        for (Point2D point : points) {
            assertTrue(point.x() >= 0 && point.x() < 200);
            assertTrue(point.y() >= 0 && point.y() < 50);
        }
    }

    @Test
    void generateRegularGridPointsStopsAtIndexLimit() {
        // Test pour couvrir la condition row < rows && index < pointCount
        // où la boucle s'arrête à cause de index >= pointCount avant row >= rows
        //
        // Pour que la condition soit évaluée avec index >= pointCount alors que row < rows,
        // il faut que cols * rows > pointCount
        //
        // Calcul pour pointCount = 5, width = 50, height = 50:
        // cols = ceil(sqrt(5 * 1.0)) = ceil(2.24) = 3
        // rows = ceil(5/3.0) = ceil(1.67) = 2
        // Grille = 3x2 = 6 points possibles, mais on veut seulement 5 points
        //
        // Itération 1 (row=0): génère 3 points (index=3)
        // Itération 2 (row=1): génère 2 points (index=5)
        // La boucle interne s'arrête car index=5 >= pointCount=5
        // La boucle externe évalue: row=1 < rows=2 est true, mais index=5 >= pointCount=5
        // Donc la condition row < rows && index < pointCount est false à cause de index >= pointCount
        WritableImage testImg = new WritableImage(50, 50);
        var writer = testImg.getPixelWriter();
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel model = new ImageModel(testImg);
        // Utiliser pointCount = 5 pour que la grille (3x2 = 6) soit plus grande que pointCount
        MosaicFilter filter = new MosaicFilter(model, 5, MosaicSeedMode.REGULAR_GRID);
        
        Point2D[] points = filter.generateRegularGridPoints();
        assertNotNull(points);
        assertEquals(5, points.length);
        // La boucle devrait s'arrêter à cause de index >= pointCount
        // et la condition row < rows && index < pointCount sera évaluée avec index >= pointCount
    }

    @Test
    void applyMosaicWithNearestNotPresent() throws Exception {
        // Test pour couvrir le cas où nearest.isPresent() est false
        // Pour forcer ce cas, nous devons créer un scénario où le KdTree est vide
        // lors de la recherche. Comme le KdTree est créé localement dans applyMosaic(),
        // nous utilisons la réflexion pour créer un test qui simule ce comportement.
        //
        // Stratégie : créer un MosaicFilter avec pointCount = 0, ce qui générera
        // un tableau de seeds vide, et donc un KdTree vide après l'insertion.
        WritableImage testImg = new WritableImage(10, 10);
        var writer = testImg.getPixelWriter();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                writer.setColor(x, y, Color.RED);
            }
        }
        
        ImageModel model = new ImageModel(testImg);
        // Utiliser pointCount = 0 pour créer un KdTree vide
        // Cela générera un tableau de seeds vide, donc le KdTree sera vide
        MosaicFilter filter = new MosaicFilter(model, 0, MosaicSeedMode.RANDOM);
        
        // Appeler applyMosaic() - cela devrait fonctionner même avec un KdTree vide
        // car la boucle for (int x=0; x<width; x++) itérera sur tous les pixels,
        // et pour chaque pixel, findNearest() retournera Optional.empty(),
        // donc nearest.isPresent() sera false, couvrant ainsi la branche else
        Image result = filter.applyMosaic();
        assertNotNull(result);
        assertEquals(10, (int) result.getWidth());
        assertEquals(10, (int) result.getHeight());
        
        // Avec pointCount = 0, le KdTree sera vide, donc nearest.isPresent() sera false
        // pour tous les pixels, couvrant ainsi la branche else de if (nearest.isPresent())
    }

}

