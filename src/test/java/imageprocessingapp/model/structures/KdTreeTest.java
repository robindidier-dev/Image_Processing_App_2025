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
}