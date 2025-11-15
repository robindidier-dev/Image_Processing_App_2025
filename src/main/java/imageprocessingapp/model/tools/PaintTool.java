package imageprocessingapp.model.tools;

import imageprocessingapp.model.ImageModel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Outil pinceau pour dessiner sur l'image.
 * 
 * Cet outil permet à l'utilisateur de dessiner des traits continus
 * en utilisant la souris. Il dessine des cercles et des lignes
 * pour créer un effet de pinceau fluide.
 * 
 * Pattern Strategy : implémente l'interface Tool pour le comportement pinceau.
 */
public class PaintTool implements Tool {

    /**
     * Contexte graphique du Canvas pour dessiner.
     * Utilisé pour tracer les formes et gérer les styles de dessin.
     */
    private final GraphicsContext gc;
    
    /**
     * Couleur utilisée pour dessiner.
     * Cette couleur sera définie depuis MainController.
     */
    private Color paintColor = Color.BLACK;
    
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
    public PaintTool(GraphicsContext gc) {
        this.gc = gc;
        // Configuration du style de dessin par défaut
        gc.setStroke(paintColor);
        gc.setFill(paintColor);
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
        gc.fillOval(prevX - brushSize/2, prevY - brushSize/2, brushSize, brushSize);
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

        gc.strokeLine(prevX, prevY, x, y);
        notifyModification();
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
        return "Pinceau";
    }
    
    /**
     * Définit la couleur du pinceau.
     * 
     * @param color La nouvelle couleur à utiliser
     */
    public void setPaintColor(Color color) {
        this.paintColor = color;
        gc.setStroke(color);
        gc.setFill(color);
    }

    /**
     * Définit la taille du pinceau.
     *
     * @param size La nouvelle taille en pixels
     */
    public void setBrushSize(double size) {
        this.brushSize = size;
        gc.setLineWidth(size);
    }
}
