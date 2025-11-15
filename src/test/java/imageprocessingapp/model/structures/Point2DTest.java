package imageprocessingapp.model.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Point2DTest {

    @Test
    void distance() {
        Point2D a = new Point2D(0, 0);
        Point2D b = new Point2D(3, 4);
        assertEquals(5.0, a.distance(b), 1e-9);
        assertEquals(25.0, a.distanceSquared(b), 1e-9);
        assertEquals(new Point2D(3, 4), b);
        assertNotEquals(a, b);
    }

    @Test
    void distanceProperties() {
        Point2D p = new Point2D(-2.5, 7.5);
        assertEquals(0.0, p.distance(p), 1e-12);
        Point2D q = new Point2D(1.5, -3.0);
        assertEquals(p.distance(q), q.distance(p), 1e-12);
    }

    @Test
    void equalsValidation() {
        Point2D p = new Point2D(1.0, 2.0);
        assertNotEquals(p, "not a point");
        assertEquals(p, new Point2D(1.0, 2.0));
        assertNotEquals(p, new Point2D(1.0, 2.0000001));
    }

    @Test
    void equalsWithNull() {
        Point2D p = new Point2D(1.0, 2.0);
        assertNotEquals(p, null);
    }

    @Test
    void equalsWithSameReference() {
        Point2D p = new Point2D(1.0, 2.0);
        assertEquals(p, p);
    }

    @Test
    void equalsWithDifferentCoordinates() {
        Point2D p1 = new Point2D(1.0, 2.0);
        Point2D p2 = new Point2D(2.0, 1.0);
        assertNotEquals(p1, p2);
    }
}