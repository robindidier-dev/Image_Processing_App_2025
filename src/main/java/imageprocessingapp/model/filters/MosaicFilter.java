package imageprocessingapp.model.filters;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.structures.KdTree;
import imageprocessingapp.model.structures.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.*;

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

        // Génération de n points aléatoires (seeds) et insertion dans le KdTree
        Point2D[] seeds = generateRandomPoints();
        KdTree kdTree = new KdTree();
        for (Point2D p : seeds) kdTree.insert(p);


        // Création d'un dictionnaire {seed : [points appartenant à une cellule]}
        // Initiation {seed1 : [seed1], etc}
        Map<Point2D, List<Point2D>> cells = new HashMap<>(seeds.length);
        for (Point2D seed : seeds) {
            List<Point2D> list = new ArrayList<>(1);
            list.add(seed); // initialisation {seed : [seed]}
            cells.put(seed, list);
        }

        // Remplissage du dictionnaire
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {

                Point2D target = new Point2D(x, y); // Pixel actuel
                Optional<Point2D> nearest = kdTree.findNearest(target); // Pixel du KdTree le plus proche

                if (nearest.isPresent()) {
                    Point2D seed = nearest.get();
                    // Test si le target = nearest pour ne pas que nearest soit deux fois dans le dictionnaire
                    if (!nearest.equals(target)) {
                        List<Point2D> cell = cells.get(seed);
                        cell.add(target);
                    }
                }
            }
        }
        // On appelle une méthode calculant la moyenne des couleurs et
        // renvoyant une WritableImage.
        return applyColor(cells);
    }

    private WritableImage applyColor(Map<Point2D, List<Point2D>> cells) {
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter writer = writableImage.getPixelWriter();

        // Itération sur les entries (couple clé valeur) du dictionnaire
        for (Map.Entry<Point2D, List<Point2D>> entry : cells.entrySet()) {
            List<Point2D> cell = entry.getValue();
            if (cell == null || cell.isEmpty()) {
                throw new NoSuchElementException("Dict mosaic : error");
            } else {
                // Calcul de la moyenne des couleurs
                double countR = 0;
                double countG = 0;
                double countB = 0;
                int total = 0;

                for (Point2D pixel: cell) {
                    int x = (int) pixel.x();
                    int y = (int) pixel.y();
                    Color c = imageModel.getPixelColor(x, y);
                    countR += c.getRed();
                    countG += c.getGreen();
                    countB += c.getBlue();
                    total++;
                }

                Color avg = new Color(countR / total, countG / total, countB / total, 1);

                // On colorie la cellule
                for (Point2D pixel: cell) {
                    int x = (int) pixel.x();
                    int y = (int) pixel.y();
                    writer.setColor(x, y, avg);
                }
            }
        }
        return writableImage;
    }
}
