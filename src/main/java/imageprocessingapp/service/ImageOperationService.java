package imageprocessingapp.service;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.operations.RotateOperation;
import imageprocessingapp.model.operations.SymmetryOperation;
import imageprocessingapp.model.operations.CropOperation;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;

/**
 * Service pour gérer les transformations d'image (rotation, symétrie, crop).
 * 
 * Extrait de MainController pour améliorer la testabilité et la séparation des responsabilités.
 */
public class ImageOperationService {
    
    private final DrawingService drawingService;
    private final ImageModel imageModel;
    private final ObjectProperty<Image> currentImageProperty;
    private final Canvas drawingCanvas;
    private final Canvas maskCanvas;
    private final CanvasStateManager stateManager;
    
    /**
     * Constructeur.
     * 
     * @param drawingService Le service de dessin
     * @param imageModel Le modèle d'image
     * @param currentImageProperty La propriété observable de l'image actuelle
     * @param drawingCanvas Le canvas de dessin
     * @param maskCanvas Le canvas de masque pour le crop
     * @param stateManager Le gestionnaire d'état du canvas
     * @param container Le container (StackPane) qui peut avoir un zoom/translation appliqué
     */
    public ImageOperationService(
            DrawingService drawingService,
            ImageModel imageModel,
            ObjectProperty<Image> currentImageProperty,
            Canvas drawingCanvas,
            Canvas maskCanvas,
            CanvasStateManager stateManager,
            Region container) {
        this.drawingService = drawingService;
        this.imageModel = imageModel;
        this.currentImageProperty = currentImageProperty;
        this.drawingCanvas = drawingCanvas;
        this.maskCanvas = maskCanvas;
        this.stateManager = stateManager;
    }
    
    /**
     * Applique une rotation à l'image.
     * 
     * @param direction La direction de rotation (CLOCKWISE ou COUNTERCLOCKWISE)
     * @return true si la rotation a été appliquée avec succès, false sinon
     */
    public boolean applyRotation(RotateOperation.Direction direction) {
        try {
            // On convertit canvas + image de fond en une image avant de la faire tourner
            WritableImage overlaySnapshot = drawingService.snapshotCanvas();

            // On prépare une variable qui contiendra la version tournée du dessin 
            WritableImage rotatedOverlay = null;

            // Si il y a des éléments sur le canvas
            if (overlaySnapshot != null) {
                ImageModel overlayModel = new ImageModel(overlaySnapshot);
                // On applique l'opération de rotation sur ce modèle, ce qui retourne l'image tournée du dessin
                rotatedOverlay = new RotateOperation(direction).apply(overlayModel);
            }

            // On prépare une variable qui contiendra la version tournée de l'image de base 
            WritableImage rotatedBase = null;

            // Si il y a une image de base chargée
            if (imageModel.hasImage()) {
                // On applique la rotation à cette image via le DrawingService (qui gère l'opération)
                rotatedBase = drawingService.applyOperation(new RotateOperation(direction));
                // On met à jour la propriété observable pour que la vue réagisse et affiche la nouvelle image
                currentImageProperty.set(rotatedBase);
                // On ajuste la taille du canvas de dessin pour coller aux nouvelles dimensions de l'image tournée
                drawingService.resizeCanvasToImage(rotatedBase);
            } else if (rotatedOverlay != null) {
                // Adapter l'overlay tourné pour tenir dans la taille d'affichage actuelle du canvas
                double maxW = drawingCanvas.getWidth();
                double maxH = drawingCanvas.getHeight();
                double ow = rotatedOverlay.getWidth();
                double oh = rotatedOverlay.getHeight();

                if (ow > 0 && oh > 0 && maxW > 0 && maxH > 0) {
                    double ratio = Math.min(maxW / ow, maxH / oh);
                    double displayW = Math.max(1.0, Math.floor(ow * ratio));
                    double displayH = Math.max(1.0, Math.floor(oh * ratio));
                    drawingCanvas.setWidth(displayW);
                    drawingCanvas.setHeight(displayH);
                } else {
                    // Fallback: dimensions brutes si invalide
                    drawingCanvas.setWidth(ow);
                    drawingCanvas.setHeight(oh);
                }
            }

            // On recrée à zéro un canevas de dessin, avec les nouvelles dimensions de l'image tournée
            drawingService.createDefaultCanvas();

            // Si on a une version tournée du dessin (overlay), on la redessine sur le nouveau canvas
            if (rotatedOverlay != null) {
                drawingService.drawImageOnCanvas(rotatedOverlay);
            }

            // On réinitialise le maskCanvas pour le crop au bonne dimensions
            if (maskCanvas != null) {
                maskCanvas.setWidth(drawingCanvas.getWidth());
                maskCanvas.setHeight(drawingCanvas.getHeight());
                maskCanvas.getGraphicsContext2D().clearRect(0, 0, maskCanvas.getWidth(), maskCanvas.getHeight());
            }
            
            // On marque l'état du canvas comme modifié, ce qui active la gestion d'undo/redos et la sauvegarde si besoin
            stateManager.markAsModified(imageModel.hasImage());

            // On retourne true pour signaler que la rotation s'est bien passée
            return true;
        } catch (IllegalStateException e) {
            showAlert("Rotation Failed", e.getMessage());
            return false;
        }
    }
    
    /**
     * Applique une symétrie à l'image.
     * 
     * @param axis L'axe de symétrie (HORIZONTAL ou VERTICAL)
     * @return true si la symétrie a été appliquée avec succès, false sinon
     */
    public boolean applySymmetry(SymmetryOperation.Axis axis) {
        try {
            // On convertit canvas + image de fond en une image avant de la symétriser
            WritableImage overlaySnapshot = drawingService.snapshotCanvas();
            WritableImage mirroredOverlay = null;

            // Si le canvas contient un dessin, appliquer la symétrie dessus
            if (overlaySnapshot != null) {
                ImageModel overlayModel = new ImageModel(overlaySnapshot);
                mirroredOverlay = new SymmetryOperation(axis).apply(overlayModel);
            }

            // Appliquer la symétrie sur l'image de fond si elle existe,
            // mettre à jour l'image courante et adapter la taille du canvas
            WritableImage flippedBase = null;
            if (imageModel.hasImage()) {
                flippedBase = drawingService.applyOperation(new SymmetryOperation(axis));
                currentImageProperty.set(flippedBase);
                drawingService.resizeCanvasToImage(flippedBase);
            }

            // On récrée un nouveau canvas vierge adapté aux nouvelles dimensions
            drawingService.createDefaultCanvas();

            // Si un dessin tourné existe, on le redessine sur le nouveau canvas
            if (mirroredOverlay != null) {
                drawingService.drawImageOnCanvas(mirroredOverlay);
            }
            
            // Marque l'état comme modifié (pour l'undo/redo et la sauvegarde)
            stateManager.markAsModified(imageModel.hasImage());

            // Succès
            return true;
        } catch (IllegalStateException e) {
            showAlert("Symmetry Failed", e.getMessage());
            return false;
        }
    }
    
    /**
     * Applique un crop sur l'image composite (fond + dessin).
     * 
     * @param cropArea La zone de crop en coordonnées d'affichage
     * @return L'image croppée ou null en cas d'erreur
     */
    public WritableImage applyCrop(Rectangle2D cropArea) {
        // Si la zone est invalide, on prévient l'utilisateur et on annule
        if (cropArea == null) {
            showAlert("Cropping Failed", "Invalid selection area.");
            return null;
        }

        try {
            // Créer une image composite (fond + canvas)
            WritableImage compositeSnapshot = createCompositeSnapshot();
            if (compositeSnapshot == null) {
                showAlert("Cropping Failed", "Unable to create composite image.");
                return null;
            }

            // Convertir les coordonnées d'affichage vers coordonnées image native
            Rectangle2D scaledCropArea = convertCropAreaToImageCoordinates(cropArea, compositeSnapshot);
            if (scaledCropArea == null) {
                showAlert("Cropping Failed", "Invalid selection area.");
                return null;
            }

            // Effectuer le crop
            WritableImage croppedImage = performCrop(compositeSnapshot, scaledCropArea);

            // Si crop réussi, on met à jour le modèle et l'affichage
            if (croppedImage != null) {
                updateImageAfterCrop(croppedImage);
            }
            
            // On retourne le résultat (null en cas d'échec)
            return croppedImage;
        } catch (Exception e) {
            showAlert("Cropping Failed", e.getMessage());
            return null;
        }
    }
    
    /**
     * Crée une image composite fusionnant le fond et le canvas.
     *
     * @return L'image composite ou null en cas d'erreur
     */
    private WritableImage createCompositeSnapshot() {
        Image compositeImage = drawingService.createCompositeImage();
        if (compositeImage == null) return null;
        // Si l'image composite est déjà une WritableImage, on la retourne telle quelle
        if (compositeImage instanceof WritableImage) {
            return (WritableImage) compositeImage;
        }

        // Sinon, on crée une nouvelle WritableImage à partir du PixelReader pour garantir la mutabilité
        return new WritableImage(
                compositeImage.getPixelReader(),
                (int) compositeImage.getWidth(),
                (int) compositeImage.getHeight()
        );
    }

    /**
     * Convertit les coordonnées de crop de l'affichage vers l'image native.
     * Applique le facteur d'échelle et le zoom/translation du container, puis clampe aux dimensions de l'image.
     *
     * @param cropArea Zone de crop en coordonnées d'affichage (local au canvas)
     * @param image Image native de référence
     * @return Zone de crop en coordonnées image ou null si invalide
     */
    private Rectangle2D convertCropAreaToImageCoordinates(Rectangle2D cropArea, WritableImage image) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double displayWidth = drawingCanvas.getWidth();
        double displayHeight = drawingCanvas.getHeight();

        // Les coordonnées de crop sont en coordonnées locales du canvas
        // Si le container a une translation, les coordonnées locales ne sont pas affectées
        // Mais si le container a un zoom, les coordonnées visuelles sont différentes
        // Il faut convertir les coordonnées locales en coordonnées dans l'espace du container
        // Puis ajuster pour le zoom du container
        double adjustedX = cropArea.getMinX();
        double adjustedY = cropArea.getMinY();
        double adjustedWidth = cropArea.getWidth();
        double adjustedHeight = cropArea.getHeight();

        // Si le container a un zoom appliqué, ajuster les coordonnées
        // Les coordonnées locales du canvas ne sont pas affectées par le zoom du parent,
        // donc on n'a pas besoin d'ajuster pour le zoom ici
        // Mais il faut ajuster si le canvas lui-même est transformé
        
        // Calculer les facteurs d'échelle image/canvas
        double scaleX = imageWidth / displayWidth;
        double scaleY = imageHeight / displayHeight;

        // Appliquer l'échelle aux coordonnées de crop
        double scaledX = adjustedX * scaleX;
        double scaledY = adjustedY * scaleY;
        double scaledWidth = adjustedWidth * scaleX;
        double scaledHeight = adjustedHeight * scaleY;

        // Clamper aux dimensions de l'image
        double x = Math.max(0, Math.min(scaledX, imageWidth - 1));
        double y = Math.max(0, Math.min(scaledY, imageHeight - 1));
        double w = Math.min(scaledWidth, imageWidth - x);
        double h = Math.min(scaledHeight, imageHeight - y);

        // Vérifier validité
        if (w <= 0 || h <= 0) return null;

        return new Rectangle2D(x, y, w, h);
    }

    /**
     * Effectue l'opération de crop sur l'image.
     *
     * @param image Image à cropper
     * @param cropArea Zone de crop en coordonnées image
     * @return Image croppée
     */
    private WritableImage performCrop(WritableImage image, Rectangle2D cropArea) {
        ImageModel snapshotModel = new ImageModel(image);
        return new CropOperation(cropArea).apply(snapshotModel);
    }

    /**
     * Met à jour l'interface après le crop : image, canvas et masque.
     *
     * @param croppedImage Image résultant du crop
     */
    private void updateImageAfterCrop(WritableImage croppedImage) {
        // Mettre à jour l'image affichée
        currentImageProperty.set(croppedImage);
        imageModel.setImage(croppedImage);

        // Redimensionner le canvas de dessin
        drawingService.resizeCanvasToImage(croppedImage);

        // Redimensionner et effacer le maskCanvas
        if (maskCanvas != null) {
            maskCanvas.setWidth(drawingCanvas.getWidth());
            maskCanvas.setHeight(drawingCanvas.getHeight());
            GraphicsContext gc = maskCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, maskCanvas.getWidth(), maskCanvas.getHeight());
        }

        // Réinitialiser le canvas de dessin
        drawingService.createDefaultCanvas();
        stateManager.markAsModified(true);
    }
    
    /**
     * Affiche une alerte avec le titre et le message donnés.
     *
     * @param title   Le titre de l'alerte
     * @param message Le message de l'alerte
     */
    private void showAlert(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}

