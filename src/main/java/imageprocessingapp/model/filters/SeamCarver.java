package imageprocessingapp.model.filters;

import imageprocessingapp.model.ImageModel;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implémente l'algorithme de Seam Carving.
 */
public class SeamCarver {


    private final EnergyCalculator energyCalculator;

    public SeamCarver() {
        this.energyCalculator = new EnergyCalculator();
    }

    /**
     * Redimensionne l'image en supprimant un nombre donné de coutures verticales.
     * Méthode principale qui orchestre tout le processus de Seam Carving.
     *
     * @param imageModel modèle contenant l'image source
     * @param numberOfSeams nombre de coutures à supprimer (= pixels à retirer en largeur)
     * @return nouvelle image redimensionnée
     */
    /**
     * Version optimisée : supprime plusieurs coutures par batch.
     */
    public WritableImage resizeOptimized(ImageModel imageModel, int numberOfSeams) {
        WritableImage currentImage = imageModel.getWritableImage();

        int batchSize = 5;  // Trouver 5 coutures avant de les supprimer

        while (numberOfSeams > 0) {
            int seamsThisBatch = Math.min(batchSize, numberOfSeams);

            // Liste pour stocker les coutures à supprimer
            List<List<Integer>> allSeams = new ArrayList<>();

            // Image virtuelle pour calculer les coutures suivantes
            WritableImage virtualImage = currentImage;

            // Trouver plusieurs coutures
            for (int i = 0; i < seamsThisBatch; i++) {
                // Calculer l'énergie sur l'image virtuelle
                ImageModel tempModel = new ImageModel(virtualImage);
                double[][] energyMap = energyCalculator.computeEnergyMap(tempModel);
                double[][] cumulativeEnergy = computeCumulativeEnergy(energyMap);

                // Trouver une couture
                List<Integer> seam = findSeam(cumulativeEnergy);
                allSeams.add(seam);

                // Supprimer la couture VIRTUELLEMENT (pas sur l'image réelle)
                // pour que la prochaine couture soit différente
                virtualImage = removeSeam(virtualImage, seam);
            }

            // Maintenant supprimer TOUTES les coutures de l'image RÉELLE
            for (List<Integer> seam : allSeams) {
                currentImage = removeSeam(currentImage, seam);
            }

            numberOfSeams -= seamsThisBatch;
            System.out.println("Batch terminé, reste " + numberOfSeams);
        }

        return currentImage;
    }





    /**
     * Calcule l'énergie cumulative en utilisant la programmation dynamique.
     *
     * @param energyMap carte d'énergie source
     * @return matrice d'énergie cumulative
     */
    public double[][] computeCumulativeEnergy(double[][] energyMap) {
        // TODO: implémenter la programmation dynamique pour la somme minimale

        int nbLignes = energyMap.length;
        int nbColonnes = energyMap[0].length;

        double[][] cumulativeEnergyMap =  new double[nbLignes][nbColonnes];

        for (int j=0;j<nbColonnes;j++) {
            cumulativeEnergyMap[0][j] = energyMap[0][j];
        }

        for (int i=1;i<nbLignes;i++) {
            cumulativeEnergyMap[i][0] = energyMap[i][0] + Math.min(cumulativeEnergyMap[i-1][0], cumulativeEnergyMap[i-1][1]);

            for (int j = 1; j<nbColonnes -1; j++) {
                double minOf3 = Math.min(Math.min(cumulativeEnergyMap[i-1][j-1], cumulativeEnergyMap[i-1][j]) , cumulativeEnergyMap[i-1][j+1]);
                cumulativeEnergyMap[i][j] = energyMap[i][j] + minOf3;
            }

            cumulativeEnergyMap[i][nbColonnes-1] = energyMap[i][nbColonnes - 1] + Math.min(cumulativeEnergyMap[i-1][nbColonnes - 2], cumulativeEnergyMap[i-1][nbColonnes - 1]);
        }


        return cumulativeEnergyMap;
    }

    /**
     * Retrouve une couture minimale à partir de la carte d'énergie cumulative.
     *
     * @param cumulativeEnergy carte cumulative générée par {@link #computeCumulativeEnergy(double[][])}
     * @return liste d'indices de colonnes (un par ligne) représentant la couture
     */
    public List<Integer> findSeam(double[][] cumulativeEnergy) {
        // TODO: implémenter le backtracking pour récupérer la couture minimale

        List<Integer> seamList = new ArrayList<>();


        int nbLignes = cumulativeEnergy.length;
        int nbColonnes = cumulativeEnergy[0].length;
        double energyMin = cumulativeEnergy[nbLignes-1][0];
        int indexEnergyMin = 0;

        // On cherche la case d'énergie minimale à la dernière ligne
        for (int j=1; j<nbColonnes; j++) {
            if (energyMin > cumulativeEnergy[nbLignes-1][j]) {
                energyMin = cumulativeEnergy[nbLignes-1][j];
                indexEnergyMin = j;
            }
        }

        seamList.add(indexEnergyMin);


        // Backtracking de l'avant-dernière ligne vers le haut
        for (int i=nbLignes-2;i>=0;i--) {
            int j = indexEnergyMin;
            // Bord gauche : on compare seulement j et j+1
            if (j == 0) {
                indexEnergyMin = cumulativeEnergy[i][0] < cumulativeEnergy[i][1] ? 0 : 1;
            }

            // Bord droit : on compare seulement j-1 et j
            else if (j == nbColonnes - 1) {
                indexEnergyMin = cumulativeEnergy[i][nbColonnes-1] < cumulativeEnergy[i][nbColonnes - 2] ? nbColonnes-1 : nbColonnes - 2;
            }

            else {
                // Centre : on compare j-1, j et j+1
                double a = cumulativeEnergy[i][j-1];
                double b = cumulativeEnergy[i][j];
                double c = cumulativeEnergy[i][j+1];

                if (a <= b && a <= c) {
                    indexEnergyMin = j - 1;
                } else if (b <= c) {
                    indexEnergyMin = j;
                } else {
                    indexEnergyMin = j + 1;
                }
            }



            seamList.add(indexEnergyMin);

        }


        // INVERSER LA LISTE pour avoir de haut en bas ✓
        Collections.reverse(seamList);

        return seamList;
    }

    /**
     * Supprime une couture de l'image en retirant un pixel par ligne.
     *
     * @param image image JavaFX source dont on veut retirer la couture
     * @param seam  liste d'indices de colonnes à supprimer (un indice par ligne)
     * @return nouvelle image avec une largeur réduite d'un pixel
     */
    public WritableImage removeSeam(WritableImage image, List<Integer> seam) {
        // TODO: implémenter le décalage des pixels pour retirer la couture

        int hauteur = (int) image.getHeight();
        int largeur = (int) image.getWidth();
        WritableImage newImage = new WritableImage(largeur - 1, hauteur);

        PixelReader reader = image.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();

        for (int i = 0; i < hauteur; i++) {
            int colToRemove = seam.get(i);

            for (int j = 0; j < colToRemove; j++) {
                writer.setColor(j, i, reader.getColor(j, i));
            }

            for (int j = colToRemove + 1; j < largeur; j++) {
                writer.setColor(j - 1, i, reader.getColor(j, i));
            }
        }

        return newImage;
    }




    /**
     * Trace la couture sur l'image au lieu de la supprimer (pour visualisation/debug).
     *
     * @param image image JavaFX source
     * @param seam  liste d'indices de colonnes de la couture à visualiser
     * @return image avec la couture tracée en rouge
     */
    public WritableImage drawSeam(WritableImage image, List<Integer> seam) {
        int hauteur = (int) image.getHeight();
        int largeur = (int) image.getWidth();

        // Créer une copie de l'image
        WritableImage newImage = new WritableImage(largeur, hauteur);

        PixelReader reader = image.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();

        // Copier tous les pixels
        for (int x = 0; x < largeur; x++) {
            for (int y = 0; y < hauteur; y++) {
                writer.setColor(x, y, reader.getColor(x, y));
            }
        }

        // Tracer la couture en rouge
        for (int i = 0; i < hauteur; i++) {
            int colToMark = seam.get(i);
            writer.setColor(colToMark, i, Color.RED);
        }

        return newImage;
    }

}



// pour suppression ligne (horizontal) : tourner l'image, effectuer changement, puis tourner dans l'autre sens