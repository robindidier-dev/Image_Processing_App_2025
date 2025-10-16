package imageprocessingapp.model.tools;

import imageprocessingapp.model.ImageModel;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;


public class PaintTool implements Tool {



    // GraphicsContext est l'objet qui permet de dessiner sur un Canvas.
    // --> fournit les méthodes pour dessiner des formes, et gérer les styles de dessin (couleurs, largeur de trait, etc.).
    private GraphicsContext gc;

    private double prevX = -1, prevY = -1;

    public PaintTool(Canvas canvas) {
        // L'idée est de fournir un canvas transparent, placé par dessus l'image importée, pour pouvoir dessiner dessus
        this.gc = canvas.getGraphicsContext2D();
    }

    @Override
    public void onMousePressed(MouseEvent event, ImageModel imageModel) {
        // Si l'on clique sans maintenir, on trace un point à la position où la souris est pressée.
        // Cela correspond aussi au démarrage d'un tracé (début du dessin).

        prevX = event.getX();
        prevY = event.getY();
        gc.fillOval(prevX, prevY, 4, 4);
    }

    @Override
    public void onMouseDragged(MouseEvent event, ImageModel imageModel) {
        // Pendant que la souris est déplacée avec le bouton enfoncé,
        // on trace un segment de ligne entre la position précédente et la position actuelle.
        // Cela permet de dessiner un tracé continu et fluide.

        double x = event.getX();
        double y = event.getY();
        if (prevX >= 0 && prevY >= 0) {
            gc.strokeLine(prevX, prevY, x, y);
        }
        prevX = x;
        prevY = y;
    }

    @Override
    public void onMouseReleased(MouseEvent event, ImageModel imageModel) {
        // On réinitialise la position précédente
        prevX = -1;
        prevY = -1;
    }

    @Override
    public String getName() {
        return "Pinceau";
    }
}
