package imageprocessingapp.model.edit;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.structures.EnergyCalculator;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implémente l'algorithme de Seam Carving.
 */
public class SeamCarver {


    private final EnergyCalculator energyCalculator;


    // Variable pour stocker la progression
    public int currentSeam = 0;
    public int totalSeams = 0;

    public SeamCarver() {
        this.energyCalculator = new EnergyCalculator();
    }

    /**
     * Redimensionne l'image en supprimant un nombre donné de coutures verticales.
     * Méthode principale qui orchestre tout le processus de Seam Carving.
     * Version optimisée finale : évite la création de nouvelle ImageModel
     * + utilise getPixels()/setPixels() au lieu de getColor()/setColor()
     *
     * @param imageModel modèle contenant l'image source
     * @param numberOfSeams nombre de coutures à supprimer (= pixels à retirer en largeur)
     * @return nouvelle image redimensionnée
     */
    public WritableImage resize(ImageModel imageModel, int numberOfSeams) {
        WritableImage currentImage = imageModel.getWritableImage();

        totalSeams = numberOfSeams;

        for (int i = 0; i < numberOfSeams; i++) {

            currentSeam = i+1;

            // 1. Calculer la carte d'énergie directement, sans créer d'ImageModel à chaque tour de boucle
            double[][] energyMap = energyCalculator.computeEnergyMap(currentImage);

            // 2. Calculer l'énergie cumulative
            double[][] cumulativeEnergy = computeCumulativeEnergy(energyMap);

            // 3. Trouver la couture de moindre énergie
            List<Integer> seam = findSeam(cumulativeEnergy);

            // 4. Supprimer la couture avec la version optimisée
            currentImage = removeSeam(currentImage, seam);
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
        int hauteur = (int) image.getHeight();
        int largeur = (int) image.getWidth();

        // Lire tous les pixels d'un coup (getPixels() est plus optimisée que getColor())
        PixelReader reader = image.getPixelReader();
        int[] pixels = new int[largeur * hauteur];
        reader.getPixels(0, 0, largeur, hauteur,
                javafx.scene.image.PixelFormat.getIntArgbInstance(),
                pixels, 0, largeur);


        int[] newPixels = new int[(largeur - 1) * hauteur];

        // Copier les pixels en excluant la couture
        for (int i = 0; i < hauteur; i++) {
            int colToRemove = seam.get(i);
            int srcPos = i * largeur;
            int dstPos = i * (largeur - 1);

            // Copier avant la couture (arraycopy() pour optimiser)
            System.arraycopy(pixels, srcPos, newPixels, dstPos, colToRemove);

            // Copier après la couture (décalé)
            System.arraycopy(pixels, srcPos + colToRemove + 1,
                    newPixels, dstPos + colToRemove,
                    largeur - colToRemove - 1);
        }

        // Écrire tous les pixels d'un coup (setPixels() est plus optimisée)
        WritableImage newImage = new WritableImage(largeur - 1, hauteur);
        PixelWriter writer = newImage.getPixelWriter();
        writer.setPixels(0, 0, largeur - 1, hauteur,
                javafx.scene.image.PixelFormat.getIntArgbInstance(),
                newPixels, 0, largeur - 1);

        return newImage;
    }
}



// pour suppression ligne (horizontal) : tourner l'image, effectuer changement, puis tourner dans l'autre sens