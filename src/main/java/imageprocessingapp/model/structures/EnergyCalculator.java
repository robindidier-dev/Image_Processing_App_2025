package imageprocessingapp.model.structures;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Calcule la carte d'énergie d'une image avec la technique des noyaux de Sobel.
 */
public class EnergyCalculator {

    // Noyaux de Sobel resp. horizontal et vertical (pour l'opération de convolution)
    static final double [][] Gx = {{-1, 0, 1},
                                   {-2, 0, 2},
                                   {-1, 0, 1}};

    static final double [][] Gy = {{-1,-2,-1},
                                   { 0, 0, 0},
                                   { 1, 2, 1}};


    /**
     * Lit la matrice en entrée aux coordonnées x,y en gérant les cas de bord.
     * Si des coordonnées dépassent, on lit le pixel valide le plus proche.
     */
    private double readGrayLevel(double[][] grayLevels, int x, int y) {
        int height = grayLevels.length;
        int width = grayLevels[0].length;

        // Gestion des cas de bord et repositionnement sur un bord intérieur
        if (x < 0) x = 0;
        if (x >= width) x = width - 1;
        if (y < 0) y = 0;
        if (y >= height) y = height - 1;

        return grayLevels[y][x];
    }

    /**
     * Prend une image en entrée et la renvoie en noir et blanc (en convention REC 709).
     */
    private double[][] getGrayLevels(WritableImage image) {

        PixelReader reader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        double[][] grayLevels = new double[height][width];

        // Lecture intégrale des couleurs de l'image
        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height,
                javafx.scene.image.PixelFormat.getIntArgbInstance(),
                pixels, 0, width);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                // Opérations bitwise pour récupérer efficacement les composantes r, g, b
                // Format de couleur : 0xFF123456 (FF alpha, 12 rouge, 34 vert, 56 bleu)
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                // Convention REC709 de proportions pour mimer perception humaine (luminance)
                grayLevels[y][x] = 0.299 * r / 255.0 + 0.587 * g / 255.0 + 0.114 * b / 255.0;
            }
        }
        return grayLevels;
    }


    /**
     * Retourne une carte d'énergie (une valeur par pixel) sous forme de matrice.
     * Version optimisée : lit tous les pixels d'un coup au lieu de getColor() pixel par pixel.
     * @param image l'image JavaFX source
     * @return matrice d'énergie de taille {@code hauteur x largeur}
     */
    public double[][] computeEnergyMap(WritableImage image) {

        // on utilise une WritableImage plutôt qu'une imageModel
        // pour pouvoir utiliser getPixels() au lieu de getPixelColor() (qui est plus coûteux)

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        double[][] energy = new double[height][width];

        // On récupère l'image en niveaux de gris
        double[][] grayLevels = getGrayLevels(image);

        // Calculer l'énergie à partir de l'image en noir et blanc
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double valueWidth = 0;
                double valueHeight = 0;

                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        // Appel à la fonction readGrayLevel pour gérer les cas de bord
                        double pixelValue = readGrayLevel(grayLevels, x + j, y + i);
                        valueWidth += Gx[1 + i][1 + j] * pixelValue;
                        valueHeight += Gy[1 + i][1 + j] * pixelValue;
                    }
                }
                // Prise en compte de la convolution verticale et horizontale pour détecter les bords dans les deux sens
                energy[y][x] = Math.sqrt(valueWidth * valueWidth + valueHeight * valueHeight);
            }
        }
        return energy;
    }
}

