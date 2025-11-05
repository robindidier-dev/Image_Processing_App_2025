package imageprocessingapp.model;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * Modèle principal de l'image.
 * 
 * Cette classe encapsule les données de l'image et fournit des méthodes
 * pour manipuler les pixels. Elle sert d'interface entre l'interface utilisateur
 * et les opérations de traitement d'image.
 * 
 * Pattern Model : représente l'état et la logique métier de l'image.
 */
public class ImageModel {
    
    /**
     * Image JavaFX actuelle.
     * Contient les données de l'image affichée.
     */
    private Image currentImage;
    
    /**
     * Image modifiable pour les opérations de dessin.
     * Utilisée pour les modifications en temps réel.
     */
    private WritableImage writableImage;
    
    /**
     * Lecteur de pixels pour lire les couleurs de l'image.
     */
    private PixelReader pixelReader;
    
    /**
     * Écrivain de pixels pour modifier l'image.
     */
    private PixelWriter pixelWriter;
    
    /**
     * Largeur de l'image en pixels.
     */
    private int width;
    
    /**
     * Hauteur de l'image en pixels.
     */
    private int height;

    /**
     * Constructeur par défaut.
     * Initialise un modèle vide.
     */
    public ImageModel() {
        // Initialisation vide, l'image sera chargée via setImage()
    }

    /**
     * Constructeur avec une image initiale.
     * 
     * @param image L'image à charger dans le modèle
     */
    public ImageModel(Image image) {
        setImage(image);
    }

    /**
     * Crée une image composite en combinant l'image de base et le canvas.
     * 
     * @param drawingCanvas Le canvas contenant les dessins
     * @return L'image composite à sauvegarder
     */
    public Image createCompositeImage(Canvas drawingCanvas) {
        if (currentImage != null) {
            return createCompositeWithImage(drawingCanvas);
        } else {
            return createCanvasOnlyImage(drawingCanvas);
        }
    }

    /**
     * Crée une image composite : image de base + canvas superposé.
     * 
     * @param drawingCanvas Le canvas contenant les dessins
     * @return L'image composite
     */
    private Image createCompositeWithImage(Canvas drawingCanvas) {
        // Créer une WritableImage pour la composition
        WritableImage compositeImage = new WritableImage(width, height);
        PixelWriter compositeWriter = compositeImage.getPixelWriter();
        
        // Copier l'image de base
        PixelReader imageReader = currentImage.getPixelReader();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color imageColor = imageReader.getColor(x, y);
                compositeWriter.setColor(x, y, imageColor);
            }
        }
        
        // Capturer le canvas avec des paramètres spéciaux pour la transparence
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(Color.TRANSPARENT);  // Fond transparent
        Image canvasSnapshot = drawingCanvas.snapshot(params, null);
        
        PixelReader canvasReader = canvasSnapshot.getPixelReader();
        
        double scaleX = (double) width / drawingCanvas.getWidth();
        double scaleY = (double) height / drawingCanvas.getHeight();
        
        // Superposer le dessin
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int canvasX = (int) (x / scaleX);
                int canvasY = (int) (y / scaleY);
                
                if (canvasX < canvasSnapshot.getWidth() && canvasY < canvasSnapshot.getHeight()) {
                    Color canvasColor = canvasReader.getColor(canvasX, canvasY);
                    
                    // Vérifier la transparence
                    if (canvasColor.getOpacity() > 0.01) {
                        compositeWriter.setColor(x, y, canvasColor);
                    }
                }
            }
        }
        
        return compositeImage;
    }

    /**
     * Crée une image à partir du canvas seul (cas du canvas par défaut).
     * 
     * @param drawingCanvas Le canvas contenant les dessins
     * @return L'image du canvas
     */
    private Image createCanvasOnlyImage(Canvas drawingCanvas) {
        return drawingCanvas.snapshot(null, null);
    }

    /**
     * Calcule les dimensions d'affichage en respectant preserveRatio.
     * 
     * @param maxWidth Largeur maximale d'affichage
     * @param maxHeight Hauteur maximale d'affichage
     * @return Un tableau [displayWidth, displayHeight]
     */
    public double[] calculateDisplayDimensions(double maxWidth, double maxHeight) {
        if (currentImage == null) {
            return new double[]{maxWidth, maxHeight};
        }
        
        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();
        
        // Calculer le ratio de redimensionnement
        double ratioX = maxWidth / imageWidth;
        double ratioY = maxHeight / imageHeight;
        double ratio = Math.min(ratioX, ratioY);
        
        return new double[]{
            imageWidth * ratio,
            imageHeight * ratio
        };
    }

    /**
     * Définit l'image du modèle et initialise les composants nécessaires.
     * 
     * @param image La nouvelle image à charger
     */
    public void setImage(Image image) {
        this.currentImage = image;
        
        if (image != null) {
            this.width = (int) image.getWidth();
            this.height = (int) image.getHeight();
            
            // Créer une copie modifiable de l'image
            this.writableImage = new WritableImage(width, height);
            this.pixelReader = image.getPixelReader();
            this.pixelWriter = writableImage.getPixelWriter();
            
            // Copier l'image originale dans l'image modifiable
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = pixelReader.getColor(x, y);
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
    }

    /**
     * Retourne l'image actuelle.
     * 
     * @return L'image JavaFX actuelle
     */
    public Image getImage() {
        return currentImage;
    }

    /**
     * Retourne l'image modifiable.
     * 
     * @return L'image modifiable pour les opérations de dessin
     */
    public WritableImage getWritableImage() {
        return writableImage;
    }

    /**
     * Retourne la largeur de l'image.
     * 
     * @return La largeur en pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Retourne la hauteur de l'image.
     * 
     * @return La hauteur en pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Lit la couleur d'un pixel à la position donnée.
     * 
     * @param x Coordonnée X du pixel
     * @param y Coordonnée Y du pixel
     * @return La couleur du pixel, ou null si les coordonnées sont invalides
     */
    public Color getPixelColor(int x, int y) {
        if (!isValidCoordinate(x, y)) return null;
        PixelReader reader = (writableImage != null) ? writableImage.getPixelReader() : pixelReader;
        return (reader != null) ? reader.getColor(x, y) : null;
    }

    /**
     * Définit la couleur d'un pixel à la position donnée.
     * 
     * @param x Coordonnée X du pixel
     * @param y Coordonnée Y du pixel
     * @param color La nouvelle couleur du pixel
     * @return true si la modification a réussi, false sinon
     */
    public boolean setPixelColor(int x, int y, Color color) {
        if (isValidCoordinate(x, y) && pixelWriter != null && color != null) {
            pixelWriter.setColor(x, y, color);
            return true;
        }
        return false;
    }

    /**
     * Dessine un cercle à la position donnée.
     * 
     * @param centerX Coordonnée X du centre du cercle
     * @param centerY Coordonnée Y du centre du cercle
     * @param radius Rayon du cercle en pixels
     * @param color Couleur du cercle
     */
    public void drawCircle(int centerX, int centerY, int radius, Color color) {
        if (pixelWriter == null || color == null) return;
        
        // Dessiner tous les pixels du cercle
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                // Vérifier si le pixel est dans le cercle
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                if (distance <= radius && isValidCoordinate(x, y)) {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
    }

    /**
     * Dessine une ligne entre deux points.
     * 
     * @param x1 Coordonnée X du premier point
     * @param y1 Coordonnée Y du premier point
     * @param x2 Coordonnée X du deuxième point
     * @param y2 Coordonnée Y du deuxième point
     * @param color Couleur de la ligne
     */
    public void drawLine(int x1, int y1, int x2, int y2, Color color) {
        if (pixelWriter == null || color == null) return;
        
        // Algorithme de Bresenham pour tracer une ligne
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        
        int x = x1, y = y1;
        
        while (true) {
            if (isValidCoordinate(x, y)) {
                pixelWriter.setColor(x, y, color);
            }
            
            if (x == x2 && y == y2) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    /**
     * Vérifie si les coordonnées sont valides pour l'image.
     * 
     * @param x Coordonnée X
     * @param y Coordonnée Y
     * @return true si les coordonnées sont dans les limites de l'image
     */
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Vérifie si le modèle contient une image valide.
     * 
     * @return true si une image est chargée, false sinon
     */
    public boolean hasImage() {
        return currentImage != null && writableImage != null;
    }

    /**
     * Réinitialise le modèle en supprimant l'image actuelle.
     */
    public void clear() {
        this.currentImage = null;
        this.writableImage = null;
        this.pixelReader = null;
        this.pixelWriter = null;
        this.width = 0;
        this.height = 0;
    }
}