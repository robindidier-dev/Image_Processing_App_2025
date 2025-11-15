package imageprocessingapp.model.structures;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class KdTreeTest {

    @Test
    void insert() {
        KdTree tree = new KdTree();
        assertTrue(tree.isEmpty());
        tree.insert(new Point2D(1, 2));
        assertEquals(1, tree.size());
        tree.insert(new Point2D(1, 2)); // doublon ignoré
        assertEquals(1, tree.size());
        tree.insert(new Point2D(-1, 5));
        assertEquals(2, tree.size());
    }

    @Test
    void findNearest() {
        KdTree tree = new KdTree();
        assertEquals(Optional.empty(), tree.findNearest(new Point2D(0, 0)));

        tree.insert(new Point2D(0, 0));
        assertEquals(new Point2D(0, 0), tree.findNearest(new Point2D(10, 10)).orElseThrow());

        tree.insert(new Point2D(5, 5));
        tree.insert(new Point2D(-2, -2));
        assertEquals(new Point2D(5, 5), tree.findNearest(new Point2D(4.9, 5.1)).orElseThrow());
    }

    @Test
    void nullHandling() {
        KdTree tree = new KdTree();
        assertThrows(NullPointerException.class, () -> tree.insert(null));
        assertThrows(NullPointerException.class, () -> tree.findNearest(null));
    }

    @Test
    void nearestColinear() {
        KdTree tree = new KdTree();
        tree.insert(new Point2D(0, 0));
        tree.insert(new Point2D(10, 0));
        tree.insert(new Point2D(20, 0));
        assertEquals(new Point2D(10, 0), tree.findNearest(new Point2D(12, 1)).orElseThrow());
    }

    @Test
    void insertDuplicatePoint() {
        KdTree tree = new KdTree();
        Point2D p = new Point2D(5, 5);
        tree.insert(p);
        assertEquals(1, tree.size());
        tree.insert(p); // Même référence
        assertEquals(1, tree.size());
        tree.insert(new Point2D(5, 5)); // Même coordonnées mais objet différent
        assertEquals(1, tree.size());
    }

    @Test
    void insertPointsOnBothSides() {
        KdTree tree = new KdTree();
        tree.insert(new Point2D(5, 5));
        tree.insert(new Point2D(3, 5)); // left
        tree.insert(new Point2D(7, 5)); // right
        tree.insert(new Point2D(5, 3)); // left (y axis)
        tree.insert(new Point2D(5, 7)); // right (y axis)
        assertEquals(5, tree.size());
    }

    @Test
    void findNearestWithPruning() {
        // Test pour couvrir la branche où planeDistSq < currentBestDistSq (exploration de la branche far)
        KdTree tree = new KdTree();
        tree.insert(new Point2D(5, 5));
        tree.insert(new Point2D(3, 5)); // left
        tree.insert(new Point2D(7, 5)); // right
        tree.insert(new Point2D(5, 3)); // left (y axis)
        tree.insert(new Point2D(5, 7)); // right (y axis)
        
        // Chercher un point qui nécessite l'exploration de la branche far
        Point2D nearest = tree.findNearest(new Point2D(6, 5.5)).orElseThrow();
        assertNotNull(nearest);
    }

    @Test
    void findNearestWithoutPruning() {
        // Test pour couvrir la branche où planeDistSq >= currentBestDistSq (pas d'exploration de la branche far)
        KdTree tree = new KdTree();
        tree.insert(new Point2D(0, 0));
        tree.insert(new Point2D(10, 0));
        tree.insert(new Point2D(0, 10));
        
        // Chercher un point très proche d'un point existant, où la branche far ne sera pas explorée
        Point2D nearest = tree.findNearest(new Point2D(0.1, 0.1)).orElseThrow();
        assertEquals(new Point2D(0, 0), nearest);
    }

    @Test
    void findNearestWithTargetOnRight() {
        // Test pour couvrir le cas où targetVal >= nodeVal (aller à droite d'abord)
        KdTree tree = new KdTree();
        tree.insert(new Point2D(5, 5));
        tree.insert(new Point2D(7, 5)); // right
        tree.insert(new Point2D(3, 5)); // left
        
        Point2D nearest = tree.findNearest(new Point2D(8, 5)).orElseThrow();
        assertEquals(new Point2D(7, 5), nearest);
    }

    @Test
    void findNearestWithNoBetterDistance() {
        // Test pour couvrir le cas où d2 >= currentBestDistSq (pas de mise à jour)
        KdTree tree = new KdTree();
        tree.insert(new Point2D(0, 0));
        tree.insert(new Point2D(10, 10)); // Plus loin
        
        // Chercher un point très proche de (0,0), donc (10,10) ne sera pas meilleur
        Point2D nearest = tree.findNearest(new Point2D(0.1, 0.1)).orElseThrow();
        assertEquals(new Point2D(0, 0), nearest);
    }

    @Test
    void isEmpty() {
        KdTree tree = new KdTree();
        assertTrue(tree.isEmpty());
        tree.insert(new Point2D(1, 1));
        assertFalse(tree.isEmpty());
    }
}