package imageprocessingapp.service;

import imageprocessingapp.model.ImageModel;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Service de gestion de l'historique undo/redo.
 * 
 * Ce service maintient deux piles :
 * - Une pile undo contenant les états précédents
 * - Une pile redo contenant les états annulés
 * 
 * Chaque état sauvegarde :
 * - L'image de base (ImageModel)
 * - Le snapshot du canvas de dessin
 * - Les dimensions du canvas
 */
public class UndoRedoService {
    
    /**
     * Représente un état de l'application (image + canvas).
     */
    private static class AppState {
        private final Image baseImage;
        private final WritableImage canvasSnapshot;
        private final double canvasWidth;
        private final double canvasHeight;
        
        public AppState(Image baseImage, WritableImage canvasSnapshot, 
                       double canvasWidth, double canvasHeight) {
            this.baseImage = baseImage;
            this.canvasSnapshot = canvasSnapshot;
            this.canvasWidth = canvasWidth;
            this.canvasHeight = canvasHeight;
        }
        
        public Image getBaseImage() {
            return baseImage;
        }
        
        public WritableImage getCanvasSnapshot() {
            return canvasSnapshot;
        }
        
        public double getCanvasWidth() {
            return canvasWidth;
        }
        
        public double getCanvasHeight() {
            return canvasHeight;
        }
    }
    
    private static final int MAX_HISTORY_SIZE = 20;
    
    private final ImageModel imageModel;
    private final DrawingService drawingService;
    private final ObjectProperty<Image> currentImageProperty;
    
    // Deque est une interface qui représente une file doublement liée (double-linked list)
    // Elle permet d'ajouter et de retirer des éléments à l'avant et à l'arrière de la file
    private final Deque<AppState> undoStack;
    private final Deque<AppState> redoStack;
    
    /**
     * Constructeur.
     * 
     * @param imageModel Le modèle d'image
     * @param drawingService Le service de dessin
     * @param currentImageProperty La propriété observable de l'image actuelle
     */
    public UndoRedoService(ImageModel imageModel, 
                          DrawingService drawingService,
                          ObjectProperty<Image> currentImageProperty) {
        this.imageModel = Objects.requireNonNull(imageModel, "imageModel");
        this.drawingService = Objects.requireNonNull(drawingService, "drawingService");
        this.currentImageProperty = Objects.requireNonNull(currentImageProperty, "currentImageProperty");
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
    }
    
    /**
     * Sauvegarde l'état actuel avant une opération.
     */
    public void saveState() {
        // Capturer l'état actuel
        Image baseImage = imageModel.getImage();
        WritableImage canvasSnapshot = drawingService.snapshotCanvas();
        double canvasWidth = drawingService.getDrawingCanvas().getWidth();
        double canvasHeight = drawingService.getDrawingCanvas().getHeight();
        
        // Créer une copie de l'image de base si elle existe
        Image baseImageCopy = null;
        if (baseImage != null) {
            baseImageCopy = cloneImage(baseImage);
        }
        
        // Créer une copie du snapshot du canvas si elle existe
        WritableImage canvasSnapshotCopy = null;
        if (canvasSnapshot != null) {
            canvasSnapshotCopy = cloneImage(canvasSnapshot);
        }
        
        // Ajouter à la pile undo
        undoStack.push(new AppState(baseImageCopy, canvasSnapshotCopy, canvasWidth, canvasHeight));
        
        // Limiter la taille de la pile
        if (undoStack.size() > MAX_HISTORY_SIZE) {
            undoStack.removeLast();
        }
        
        // Vider la pile redo quand on fait une nouvelle opération
        redoStack.clear();
    }
    
    /**
     * Restaure l'état précédent (undo).
     */
    public void undo() {
        if (!canUndo()) {
            return;
        }
        
        // Sauvegarder l'état actuel dans redo avant de restaurer
        Image currentBaseImage = imageModel.getImage();
        WritableImage currentCanvasSnapshot = drawingService.snapshotCanvas();
        double currentCanvasWidth = drawingService.getDrawingCanvas().getWidth();
        double currentCanvasHeight = drawingService.getDrawingCanvas().getHeight();
        
        Image currentBaseImageCopy = null;
        if (currentBaseImage != null) {
            currentBaseImageCopy = cloneImage(currentBaseImage);
        }
        
        WritableImage currentCanvasSnapshotCopy = null;
        if (currentCanvasSnapshot != null) {
            currentCanvasSnapshotCopy = cloneImage(currentCanvasSnapshot);
        }
        
        redoStack.push(new AppState(currentBaseImageCopy, currentCanvasSnapshotCopy, 
                                   currentCanvasWidth, currentCanvasHeight));
        
        // Restaurer l'état précédent
        AppState previousState = undoStack.pop();
        restoreState(previousState);
    }
    
    /**
     * Restaure l'état annulé (redo).
     */
    public void redo() {
        if (!canRedo()) {
            return;
        }
        
        // Sauvegarder l'état actuel dans undo avant de restaurer
        Image currentBaseImage = imageModel.getImage();
        WritableImage currentCanvasSnapshot = drawingService.snapshotCanvas();
        double currentCanvasWidth = drawingService.getDrawingCanvas().getWidth();
        double currentCanvasHeight = drawingService.getDrawingCanvas().getHeight();
        
        Image currentBaseImageCopy = null;
        if (currentBaseImage != null) {
            currentBaseImageCopy = cloneImage(currentBaseImage);
        }
        
        WritableImage currentCanvasSnapshotCopy = null;
        if (currentCanvasSnapshot != null) {
            currentCanvasSnapshotCopy = cloneImage(currentCanvasSnapshot);
        }
        
        undoStack.push(new AppState(currentBaseImageCopy, currentCanvasSnapshotCopy, 
                                   currentCanvasWidth, currentCanvasHeight));
        
        // Restaurer l'état suivant
        AppState nextState = redoStack.pop();
        restoreState(nextState);
    }
    
    /**
     * Restaure un état donné.
     * 
     * @param state L'état à restaurer
     */
    private void restoreState(AppState state) {
        // Restaurer l'image de base
        if (state.getBaseImage() != null) {
            imageModel.setImage(state.getBaseImage());
            currentImageProperty.set(state.getBaseImage());
        } else {
            imageModel.clear();
            currentImageProperty.set(null);
        }
        
        // Restaurer le canvas
        drawingService.getDrawingCanvas().setWidth(state.getCanvasWidth());
        drawingService.getDrawingCanvas().setHeight(state.getCanvasHeight());
        drawingService.createDefaultCanvas();
        
        // Restaurer le snapshot du canvas si disponible
        if (state.getCanvasSnapshot() != null) {
            drawingService.drawImageOnCanvas(state.getCanvasSnapshot());
        }
    }
    
    /**
     * Clone une image JavaFX.
     * 
     * @param source L'image source
     * @return Une copie de l'image
     */
    private WritableImage cloneImage(Image source) {
        if (source == null) {
            return null;
        }
        
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage copy = new WritableImage(width, height);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                copy.getPixelWriter().setColor(x, y, source.getPixelReader().getColor(x, y));
            }
        }
        
        return copy;
    }
    
    /**
     * Vérifie si une opération undo est possible.
     * 
     * @return true si undo est possible, false sinon
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Vérifie si une opération redo est possible.
     * 
     * @return true si redo est possible, false sinon
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Vide les piles undo et redo.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}

