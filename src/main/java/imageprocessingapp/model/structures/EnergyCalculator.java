package imageprocessingapp.model.structures;

import imageprocessingapp.model.ImageModel;
import javafx.scene.paint.Color;
import static java.lang.Math.sqrt;

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
     * Retourne le niveau de gris du pixel et traite les cas de bord
     *
     * @param imageModel modèle contenant l'image source
     * @return double entre 0 et 1
     */
    private double pixel(ImageModel imageModel, int x, int y) {
        int width = imageModel.getWidth();
        int height = imageModel.getHeight();

        // Si les coordonnées fournies débordent, on reprend les bords pour ne pas avoir d'incohérence de gradient
        if (x < 0) {
            x = 0;
        }
        if (x >= width) {
            x = width - 1;
        }
        if (y < 0) {
            y = 0;
        }

        if (y >= height) {
            y = height - 1;
        }

        Color c = imageModel.getPixelColor(x, y);
            // Niveau de gris via Rec. 709 pour mimer perception de l'oeil
            return 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
    }


    /**
     * Retourne une carte d'énergie (une valeur par pixel) sous forme de matrice.
     *
     * @param imageModel modèle contenant l'image source
     * @return matrice d'énergie de taille {@code hauteur x largeur}
     */
    public double[][] computeEnergyMap(ImageModel imageModel) {
        int width = imageModel.getWidth();
        int height = imageModel.getHeight();

        double[][] energy = new double[height][width];

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {


                // Energie horizontale (convolution avec Gx)
                double valueWidth = 0;
                // Energie verticale (convolution avec Gy)
                double valueHeight = 0;

                for (int i=-1; i<2; i++) {
                    for (int j=-1; j<2; j++) {
                        valueWidth += Gx[1+i][1+j] * pixel(imageModel, x+j, y+i); // convolution de Gx par le voisinage du pixel
                        valueHeight += Gy[1+i][1+j] * pixel(imageModel, x+j, y+i); // convolution de Gx par le voisinage du pixel
                    }
                }
                // Combinaison des energies dans les deux sens
                energy[y][x] = sqrt(valueWidth*valueWidth + valueHeight*valueHeight);
            }
        }
        return energy;
    }
}

