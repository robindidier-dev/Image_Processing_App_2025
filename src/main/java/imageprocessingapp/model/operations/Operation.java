package imageprocessingapp.model.operations;

import imageprocessingapp.model.ImageModel;
import javafx.scene.image.WritableImage;

/**
 * Représente une transformation applicative sur une image.
 *
 * Les implémentations doivent rester agnostiques de l'interface utilisateur
 * et opérer uniquement via l'API fournie par {@link ImageModel}.
 */
@FunctionalInterface
public interface Operation {

    /**
     * Applique l'opération sur le modèle et retourne l'image résultante.
     *
     * @param imageModel modèle contenant l'image à transformer
     * @return l'image transformée, généralement une nouvelle instance de {@link WritableImage}
     * @throws IllegalStateException si l'image n'est pas disponible ou si l'opération ne peut être exécutée
     */
    WritableImage apply(ImageModel imageModel);
}

