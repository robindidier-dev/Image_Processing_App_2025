package imageprocessingapp.model.filters;

import imageprocessingapp.model.ImageModel;

/**
 * Calcule la carte d'énergie d'une image.
 */
public class EnergyCalculator {

    /**
     * Retourne une carte d'énergie (une valeur par pixel) sous forme de matrice.
     *
     * @param imageModel modèle contenant l'image source
     * @return matrice d'énergie de taille {@code hauteur x largeur}
     */
    public double[][] computeEnergyMap(ImageModel imageModel) {
        // TODO: implémenter le calcul du gradient (différences de pixels voisins)
        return new double[0][0];
    }
}

