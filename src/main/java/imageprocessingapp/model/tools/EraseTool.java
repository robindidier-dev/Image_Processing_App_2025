package imageprocessingapp.model.tools;

import imageprocessingapp.model.ImageModel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Outil gomme pour effacer.
 *
 * Même comportement que PaintTool.
 *
 * Pattern Strategy : implémente l'interface Tool pour le comportement pinceau.
 */
public class EraseTool implements Tool {

    /**
     * Contexte graphique du Canvas pour dessiner.
     * Utilisé pour tracer les formes et gérer les styles de dessin.
     */
    private final GraphicsContext gc;

    /**
     * Taille du pinceau en pixels.
     */
    private double brushSize;

    /**
     * Coordonnées précédentes de la souris pour tracer des lignes continues.
     * Initialisées à -1 pour indiquer qu'aucun point précédent n'existe.
     */
    private double prevX = -1, prevY = -1;

    // Callback pour notifier les modifications
    private Runnable onModificationCallback;

    /**
     * Constructeur de l'outil pinceau.
     *
     * @param gc Le GraphicsContext sur lequel dessiner
     */
    public EraseTool(GraphicsContext gc) {
        this.gc = gc;
        gc.setStroke(Color.WHITE);
        gc.setFill(Color.WHITE);
        gc.setLineWidth(brushSize);
    }

    /**
     * Définit le callback appelé quand une modification est effectuée.
     *
     * @param callback Le callback à appeler
     */
    public void setOnModificationCallback(Runnable callback) {
        this.onModificationCallback = callback;
    }

    /**
     * Notifie qu'une modification a été effectuée.
     */
    private void notifyModification() {
        if (onModificationCallback != null) {
            onModificationCallback.run();
        }
    }



    /**
     * Permet rendre des lignes transparentes (uniquement des rectangles nativement)
     * Interpole entre (x1,y1) et (x2,y2) et efface des rectangles centrés le long du segment.
     * steps = (int) Math.ceil(distance) pour couvrir chaque pixel environ, simple et efficace.
     */
    private void eraseRectLine(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.hypot(dx, dy);
        int steps = Math.max(1, (int) Math.ceil(dist));

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double xi = x1 + t * dx;
            double yi = y1 + t * dy;
            gc.clearRect(xi - brushSize / 2.0, yi - brushSize / 2.0, brushSize, brushSize);
        }
    }


    /**
     * Gère le début du dessin quand l'utilisateur appuie sur la souris.
     *
     * @param event L'événement de souris contenant les coordonnées
     * @param imageModel Le modèle de l'image (non utilisé pour cet outil)
     */
    @Override
    public void onMousePressed(MouseEvent event, ImageModel imageModel) {
        // Si l'on clique sans maintenir, on trace un point à la position où la souris est pressée.
        // Cela correspond aussi au démarrage d'un tracé (début du dessin).

        prevX = event.getX();
        prevY = event.getY();
        if (imageModel.hasImage()) {
            // Gomme transparente sur l'image
            gc.clearRect(prevX - brushSize/2, prevY - brushSize/2, brushSize, brushSize);
        } else {
            // Gomme "blanche" sur le canvas vide
            gc.setFill(Color.WHITE);
            gc.fillOval(prevX - brushSize/2, prevY - brushSize/2, brushSize, brushSize);
        }
        notifyModification();
    }

    /**
     * Gère le dessin continu pendant que l'utilisateur fait glisser la souris.
     *
     * @param event L'événement de souris contenant les coordonnées
     * @param imageModel Le modèle de l'image (non utilisé pour cet outil)
     */
    @Override
    public void onMouseDragged(MouseEvent event, ImageModel imageModel) {
        // Pendant que la souris est déplacée avec le bouton enfoncé,
        // on trace un segment de ligne entre la position précédente et la position actuelle.
        // Cela permet de dessiner un tracé continu et fluide.

        double x = event.getX();
        double y = event.getY();

        // Lisse le tracé
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        if (prevX >= 0 && prevY >= 0) {

            if (imageModel.hasImage()) {
                // Gomme transparente sur l'image
                eraseRectLine(prevX, prevY, x, y);
            } else {
                // Gomme "blanche" sur le canvas vide
                gc.strokeLine(prevX, prevY, x, y);
            }
            notifyModification();
        }
        prevX = x;
        prevY = y;
    }

    /**
     * Gère la fin du dessin quand l'utilisateur relâche la souris.
     *
     * @param event L'événement de souris (non utilisé)
     * @param imageModel Le modèle de l'image (non utilisé)
     */
    @Override
    public void onMouseReleased(MouseEvent event, ImageModel imageModel) {
        // On réinitialise la position précédente
        prevX = -1;
        prevY = -1;
    }

    @Override
    public String getName() {
        return "Gomme";
    }

    /**
     * Définit la taille de la gomme.
     *
     * @param size La nouvelle taille en pixels
     */
    public void setBrushSize(double size) {
        this.brushSize = size;
        gc.setLineWidth(size);
    }
}
