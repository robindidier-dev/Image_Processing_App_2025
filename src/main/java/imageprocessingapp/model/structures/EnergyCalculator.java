package imageprocessingapp.model.structures;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Calcule la carte d'énergie d'une image.
 */
public class EnergyCalculator {

    // Noyaux de Sobel resp. horizontal et vertical
    static final double [][] Gx = {{-1, 0, 1},
                                   {-2, 0, 2},
                                   {-1, 0, 1}};

    static final double [][] Gy = {{-1,-2,-1},
                                   { 0, 0, 0},
                                   { 1, 2, 1}};


    /**
     * Retourne une carte d'énergie (une valeur par pixel) sous forme de matrice.
     * Version optimisée : lit tous les pixels d'un coup au lieu de getColor() pixel par pixel.
     * @param image l'image JavaFX source
     * @return matrice d'énergie de taille {@code hauteur x largeur}
     */
    public double[][] computeEnergyMap(WritableImage image) {

        // on utilise une WritableImage plutôt qu'une imageModel pour pouvoir utiliser getPixels() au lieu de getPixelColor() (qui est plus coûteuse)

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        double[][] energy = new double[height][width];

        // Lire tous les pixels d'un coup (énorme gain de temps)
        PixelReader reader = image.getPixelReader();
        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height,
                javafx.scene.image.PixelFormat.getIntArgbInstance(),
                pixels, 0, width);

        // Précalculer les niveaux de gris pour tous les pixels
        double[][] grayLevels = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                grayLevels[y][x] = 0.299 * r / 255.0 + 0.587 * g / 255.0 + 0.114 * b / 255.0;
            }
        }

        // Calculer l'énergie avec les niveaux de gris précalculés
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double valueWidth = 0;
                double valueHeight = 0;

                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        double pixelValue = getGrayLevel(grayLevels, width, height, x + j, y + i);
                        valueWidth += Gx[1 + i][1 + j] * pixelValue;
                        valueHeight += Gy[1 + i][1 + j] * pixelValue;
                    }
                }

                energy[y][x] = Math.sqrt(valueWidth * valueWidth + valueHeight * valueHeight);
            }
        }

        return energy;
    }

    /**
     * Récupère le niveau de gris avec gestion des bords.
     */
    private double getGrayLevel(double[][] grayLevels, int width, int height, int x, int y) {
        if (x < 0) x = 0;
        if (x >= width) x = width - 1;
        if (y < 0) y = 0;
        if (y >= height) y = height - 1;

        return grayLevels[y][x];
    }
}

