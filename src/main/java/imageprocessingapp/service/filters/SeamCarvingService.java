package imageprocessingapp.service.filters;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.filters.EnergyCalculator;
import imageprocessingapp.model.filters.SeamCarver;

import java.util.List;
import java.util.Objects;

/**
 * Orchestration haut-niveau des opérations de Seam Carving.
 */
public class SeamCarvingService {

    private final EnergyCalculator energyCalculator;
    private final SeamCarver seamCarver;

    public SeamCarvingService(EnergyCalculator energyCalculator, SeamCarver seamCarver) {
        this.energyCalculator = energyCalculator;
        this.seamCarver = seamCarver;
    }

    /**
     * Supprime une couture verticale de l'image du modèle.
     *
     * @param imageModel modèle porteur de l'image à modifier
     */
    public void removeVerticalSeam(ImageModel imageModel) {
        // TODO: récupérer la carte d'énergie, trouver la couture minimale et mettre à jour l'image
    }

    /**
     * Supprime {@code count} coutures verticales.
     *
     * @param imageModel modèle porteur de l'image à modifier
     * @param count      nombre de coutures à retirer
     */
    public void removeVerticalSeams(ImageModel imageModel, int count) {
        // TODO: boucler sur {@link #removeVerticalSeam(ImageModel)} et gérer la progression
    }
}

