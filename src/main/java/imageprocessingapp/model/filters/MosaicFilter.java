package imageprocessingapp.model.filters;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.structures.KdTree;
import imageprocessingapp.model.structures.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.Optional;

import java.util.Random;

public class MosaicFilter {

    private ImageModel imageModel;

    // Nombre de zones de couleur dans la mosaïque
    private int pointCount;

    // Dimensions de l'image
    private final int width;
    private final int height;


    public MosaicFilter(ImageModel imageModel, int pointCount) {
        this.imageModel = imageModel;
        this.pointCount = pointCount;
        this.width = imageModel.getWidth();
        this.height = imageModel.getHeight();
    }

    /**
     * Retourne un tableau de pointCount points aléatoires aux coordonnées
     * incluses dans l'image.
     *
     * @return Un tableau de Point2D.
     */
    public Point2D[] generateRandomPoints() {

        Point2D[] points = new Point2D[pointCount];

        for (int i=0; i<pointCount; i++) {

            Random rand = new Random();
            // Entier dans [[0, width-1]]
            int x = rand.nextInt(width);
            // Entier dans [[0, height-1]]
            int y = rand.nextInt(height);
            // Stocké en double, mais basé sur des entiers
            Point2D point = new Point2D(x, y);
            points[i] = point;
        }
        return points;
    }


    public Image applyMosaic() {

        WritableImage writableImage = new WritableImage(width, height);
        // Récupérer le PixelWriter de la writableImage
        PixelWriter writer = writableImage.getPixelWriter();

        Point2D[] points = generateRandomPoints();

        // Ajout des points aléatoires dans le KdTree
        KdTree kdTree = new KdTree();
        for (Point2D p : points) kdTree.insert(p);

        // Couleur par défaut
        Color color = Color.BLACK;

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {

                Point2D target = new Point2D(x, y); // Pixel actuel

                Optional<Point2D> nearest = kdTree.findNearest(target);
                if (nearest.isPresent()) {
                    Point2D point = nearest.get();
                    int xNearest = (int) point.x();
                    int yNearest = (int) point.y();
                    color = imageModel.getPixelColor(xNearest, yNearest);
                }
                // On colorie le pixel (x, y)
                writer.setColor(x, y, color);
            }
        }
        return writableImage;
    }
}
