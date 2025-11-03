package imageprocessingapp.model.structures;

public final class Point2D {
    private final double x;
    private final double y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Getters
    public double x() { return x; }
    public double y() { return y; }

    /**
     * Calcule la distance entre deux points.
     * @param other Le point avec lequel calculer la distance
     * @return La distance entre les deux points
     */
    public double distance(Point2D other) {
        return Math.sqrt(distanceSquared(other));
    }

    /**
     * Calcule la distance au carré entre deux points.
     * @param other Le point avec lequel calculer la distance au carré
     * @return La distance au carré entre les deux points
     */
    public double distanceSquared(Point2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    /**
     * Vérifie si deux points sont égaux.
     * @param o L'objet avec lequel vérifier l'égalité
     * @return true si les deux points sont égaux, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point2D)) return false;
        Point2D point2D = (Point2D) o;
        return Double.compare(point2D.x, x) == 0 &&
               Double.compare(point2D.y, y) == 0;
    }
}