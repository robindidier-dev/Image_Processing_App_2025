package imageprocessingapp.model.tools;

import javafx.scene.input.MouseEvent;
import imageprocessingapp.model.ImageModel;

/**
 * Interface définissant le contrat pour tous les outils de dessin de l'application.
 * 
 * Cette interface permet d'implémenter différents outils de dessin
 * (pinceau, pipette, gomme, etc.) de manière interchangeable.
 * 
 * Chaque outil doit gérer les événements de souris et interagir avec l'ImageModel
 * pour modifier l'image selon son comportement spécifique.
 */

public interface Tool {
    
    public void onMousePressed(MouseEvent event, ImageModel imageModel);
    public void onMouseDragged(MouseEvent event, ImageModel imageModel);
    public void onMouseReleased(MouseEvent event, ImageModel imageModel);

    String getName(); // Retourne le nom de l'outil (ex: "Pinceau", "Pipette", "Gomme")

}