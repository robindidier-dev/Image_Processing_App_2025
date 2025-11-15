package imageprocessingapp.controller;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.Tool;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

/**
 * Gère les événements souris et clavier.
 * 
 * Extrait de MainController pour améliorer la testabilité et la séparation des responsabilités.
 */
public class EventHandlerManager {
    
    private final Canvas drawingCanvas;
    private final ImageView imageView;
    private final ObjectProperty<Tool> activeToolProperty;
    private final ImageModel imageModel;
    
    // Callbacks pour les actions
    private Runnable onSaveAction;
    private Runnable onOpenAction;
    private Runnable onNewAction;
    private Runnable onCloseAction;
    private Runnable onUndoAction;
    private Runnable onRedoAction;
    private Runnable onDrawingStart;
    private Runnable onCropComplete;
    
    /**
     * Constructeur.
     * 
     * @param drawingCanvas Le canvas de dessin
     * @param imageView L'ImageView pour obtenir la scène
     * @param activeToolProperty La propriété observable de l'outil actif
     * @param imageModel Le modèle d'image
     */
    public EventHandlerManager(
            Canvas drawingCanvas,
            ImageView imageView,
            ObjectProperty<Tool> activeToolProperty,
            ImageModel imageModel) {
        this.drawingCanvas = drawingCanvas;
        this.imageView = imageView;
        this.activeToolProperty = activeToolProperty;
        this.imageModel = imageModel;
    }
    
    /**
     * Configure les gestionnaires d'événements souris.
     */
    public void setupMouseHandlers() {
        drawingCanvas.setOnMousePressed(this::handleMousePressed);
        drawingCanvas.setOnMouseDragged(this::handleMouseDragged);
        drawingCanvas.setOnMouseReleased(this::handleMouseReleased);
    }
    
    /**
     * Configure les raccourcis clavier (Ctrl+S/Cmd+S, etc.).
     */
    public void setupKeyboardShortcuts() {
        Scene scene = imageView.getScene();
        if (scene != null) {
            scene.setOnKeyPressed(event -> {
                if (event.isShortcutDown() && event.getCode() == KeyCode.S) {
                    if (onSaveAction != null) {
                        onSaveAction.run();
                    }
                    event.consume();
                } else if (event.isShortcutDown() && event.getCode() == KeyCode.O) {
                    if (onOpenAction != null) {
                        onOpenAction.run();
                    }
                    event.consume();
                } else if (event.isShortcutDown() && event.getCode() == KeyCode.N) {
                    if (onNewAction != null) {
                        onNewAction.run();
                    }
                    event.consume();
                } else if (event.isShortcutDown() && event.getCode() == KeyCode.W) {
                    if (onCloseAction != null) {
                        onCloseAction.run();
                    }
                    event.consume();
                } else if (event.isShortcutDown() && event.getCode() == KeyCode.Z) {
                    if (onUndoAction != null) {
                        onUndoAction.run();
                    }    
                    event.consume();
                } else if (event.isShortcutDown() && event.getCode() == KeyCode.Y) {
                    if (onRedoAction != null) {
                        onRedoAction.run();
                    }
                    event.consume();
                }
            });
        }
    }
    
    /**
     * Définit le callback pour l'action de sauvegarde.
     * 
     * @param action Le callback à exécuter
     */
    public void setOnSaveAction(Runnable action) {
        this.onSaveAction = action;
    }
    
    /**
     * Définit le callback pour l'action d'ouverture.
     * 
     * @param action Le callback à exécuter
     */
    public void setOnOpenAction(Runnable action) {
        this.onOpenAction = action;
    }
    
    /**
     * Définit le callback pour l'action de nouveau canvas.
     * 
     * @param action Le callback à exécuter
     */
    public void setOnNewAction(Runnable action) {
        this.onNewAction = action;
    }
    
    /**
     * Définit le callback pour l'action de fermeture.
     * 
     * @param action Le callback à exécuter
     */
    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
    }
    
    /**
     * Définit le callback pour l'action undo.
     * 
     * @param action Le callback à exécuter
     */
    public void setOnUndoAction(Runnable action) {
        this.onUndoAction = action;
    }
    
    /**
     * Définit le callback pour l'action redo.
     * 
     * @param action Le callback à exécuter
     */
    public void setOnRedoAction(Runnable action) {
        this.onRedoAction = action;
    }
    
    /**
     * Définit le callback appelé au début d'une opération de dessin.
     * 
     * @param action Le callback à exécuter
     */
    public void setOnDrawingStart(Runnable action) {
        this.onDrawingStart = action;
    }
    
    /**
     * Gère l'événement de pression de souris.
     * 
     * @param event L'événement de souris
     */
    private void handleMousePressed(MouseEvent event) {
        Tool tool = activeToolProperty.get();
        if (tool != null) {
            // Notifier le début du dessin pour sauvegarder l'état
            if (onDrawingStart != null) {
                onDrawingStart.run();
            }
            tool.onMousePressed(event, imageModel);
        }
    }
    
    /**
     * Gère l'événement de glissement de souris.
     * 
     * @param event L'événement de souris
     */
    private void handleMouseDragged(MouseEvent event) {
        Tool tool = activeToolProperty.get();
        if (tool != null) {
            tool.onMouseDragged(event, imageModel);
        }
    }
    
    /**
     * Gère l'événement de relâchement de souris.
     * 
     * @param event L'événement de souris
     */
    private void handleMouseReleased(MouseEvent event) {
        Tool tool = activeToolProperty.get();
        if (tool != null) {
            tool.onMouseReleased(event, imageModel);
            
            // Si c'est un CropTool, déclencher l'application du crop après la sélection
            if (tool instanceof imageprocessingapp.model.tools.edit.CropTool) {
                if (onCropComplete != null) {
                    onCropComplete.run();
                }
            }
        }
    }
    
    /**
     * Définit le callback appelé après la sélection d'une zone de crop.
     * 
     * @param action Le callback à exécuter
     */
    public void setOnCropComplete(Runnable action) {
        this.onCropComplete = action;
    }
}

