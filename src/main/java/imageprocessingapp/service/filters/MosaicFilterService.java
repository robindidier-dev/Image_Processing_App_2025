package imageprocessingapp.service.filters;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.filters.MosaicFilter;
import imageprocessingapp.model.filters.MosaicFilter.MosaicSeedMode;
import javafx.scene.image.Image;

/**
 * Service utilitaire pour appliquer l'effet mosaïque.
 * Permet de centraliser l'orchestration entre le modèle d'image
 * et le filtre, afin de garder les contrôleurs légers.
 */
public class MosaicFilterService {

    /**
     * Applique l'effet mosaïque sur le modèle donné.
     *
     * @param imageModel modèle de l'image sur laquelle appliquer l'effet
     * @param pointCount nombre de cellules (points seeds) de la mosaïque
     * @return l'image générée par le filtre mosaïque
     * @throws IllegalArgumentException si le modèle est nul ou ne contient pas d'image
     */
    public Image applyMosaic(ImageModel imageModel, int pointCount, MosaicSeedMode mode) {
        if (imageModel == null || !imageModel.hasImage()) {
            throw new IllegalArgumentException("ImageModel must contain an image before applying mosaic.");
        }
        if (pointCount < 0) {
            throw new IllegalArgumentException("pointCount must be positive.");
        }
        MosaicSeedMode effectiveMode = mode != null ? mode : MosaicFilter.MosaicSeedMode.RANDOM;
        return new MosaicFilter(imageModel, pointCount, effectiveMode).applyMosaic();
    }
}

