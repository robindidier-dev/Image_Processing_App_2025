package imageprocessingapp.model.filters;

import java.util.List;

/**
 * Implémente l'algorithme de Seam Carving.
 */
public class SeamCarver {

    /**
     * Calcule l'énergie cumulative en utilisant la programmation dynamique.
     *
     * @param energyMap carte d'énergie source
     * @return matrice d'énergie cumulative
     */
    public double[][] computeCumulativeEnergy(double[][] energyMap) {
        // TODO: implémenter la programmation dynamique pour la somme minimale
        return new double[0][0];
    }

    /**
     * Retrouve une couture minimale à partir de la carte d'énergie cumulative.
     *
     * @param cumulativeEnergy carte cumulative générée par {@link #computeCumulativeEnergy(double[][])}
     * @return liste d'indices de colonnes (un par ligne) représentant la couture
     */
    public List<Integer> findSeam(double[][] cumulativeEnergy) {
        // TODO: implémenter le backtracking pour récupérer la couture minimale
        return List.of();
    }

    /**
     * Supprime une couture de l'image représentée comme matrice de pixels.
     *
     * @param pixels pixels de l'image (hauteur x largeur x canaux)
     * @param seam   indices de colonnes à supprimer (une colonne par ligne)
     * @return nouvelle matrice de pixels sans la couture
     */
    public int[][][] removeSeam(int[][][] pixels, List<Integer> seam) {
        // TODO: implémenter le décalage des pixels pour retirer la couture
        return new int[0][0][0];
    }
}

