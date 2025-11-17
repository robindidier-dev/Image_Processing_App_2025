package imageprocessingapp.service;

import imageprocessingapp.model.ImageModel;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Service pour gérer les opérations de fichiers (ouvrir, sauvegarder, nouveau canvas).
 * 
 * Extrait de MainController pour améliorer la testabilité et la séparation des responsabilités.
 */
public class FileManagementService {
    
    private final DrawingService drawingService;
    private final ImageModel imageModel;
    private final CanvasStateManager stateManager;
    private final ObjectProperty<Image> currentImageProperty;
    private File sourceFile;
    
    /**
     * Constructeur.
     * 
     * @param drawingService Le service de dessin
     * @param imageModel Le modèle d'image
     * @param stateManager Le gestionnaire d'état du canvas
     * @param currentImageProperty La propriété observable de l'image actuelle
     */
    public FileManagementService(
            DrawingService drawingService,
            ImageModel imageModel,
            CanvasStateManager stateManager,
            ObjectProperty<Image> currentImageProperty) {
        this.drawingService = drawingService;
        this.imageModel = imageModel;
        this.stateManager = stateManager;
        this.currentImageProperty = currentImageProperty;
    }
    
    /**
     * Ouvre une image depuis le système de fichiers.
     * 
     * @param parentStage La fenêtre parente pour le FileChooser
     * @return true si une image a été chargée, false sinon
     */
    public boolean openImage(Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        
        // Définir les extensions acceptées
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Images", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Ouverture du FileChooser
        File selectedFile = fileChooser.showOpenDialog(parentStage);
        if (selectedFile != null) {
            return loadImageFromFile(selectedFile);
        }
        return false;
    }
    
    /**
     * Charge une image depuis un fichier.
     * 
     * @param file Le fichier à charger
     * @return true si l'image a été chargée avec succès, false sinon
     */
    public boolean loadImageFromFile(File file) {
        // Vérifier l'extension du fichier
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
            showAlert("Invalid Extension", 
                    "Please select a file with .png, .jpg or .jpeg extension.");
            return false;
        }
        
        try {
            // Charger l'image
            Image image = new Image(file.toURI().toString());
            currentImageProperty.set(image);
            
            // Mettre à jour le modèle d'image
            imageModel.setImage(image);
            
            // Redimensionner le Canvas pour correspondre à l'image
            drawingService.resizeCanvasToImage(image);

            // Réinitialiser le canvas pour qu'il soit transparent
            drawingService.createDefaultCanvas();
            
            // Stocker le fichier source pour la sauvegarde
            sourceFile = file;
            
            // Réinitialiser les flags de modification
            stateManager.markAsSaved();
            
            return true;
        } catch (Exception e) {
            showAlert("Load Error", "Unable to load image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sauvegarde l'image actuelle avec les modifications effectuées.
     * 
     * @param parentStage La fenêtre parente pour le FileChooser
     * @return true si l'image a été sauvegardée, false sinon
     */
    public boolean saveImage(Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");

        // Générer le nom du fichier à enregistrer par défaut
        String defaultFileName = "dessin.png";
        if (sourceFile != null) {
            String sourceName = sourceFile.getName();
            // Séparer le nom et l'extension
            int dotIndex = sourceName.lastIndexOf('.');
            if (dotIndex > 0) {
                String nameWithoutExt = sourceName.substring(0, dotIndex);
                String extension = sourceName.substring(dotIndex); // inclut le point
                defaultFileName = nameWithoutExt + "_edited" + extension;
            }
        }
        fileChooser.setInitialFileName(defaultFileName);

        // Définir le répertoire où enregistrer (même répertoire que le fichier source)
        if (sourceFile != null && sourceFile.getParentFile() != null) {
            fileChooser.setInitialDirectory(sourceFile.getParentFile());
        }

        File selectedFile = fileChooser.showSaveDialog(parentStage);
        if (selectedFile != null) {
            return saveImageToFile(selectedFile);
        }
        return false;
    }
    
    /**
     * Sauvegarde l'image dans un fichier.
     * 
     * @param file Le fichier de destination
     * @return true si l'image a été sauvegardée avec succès, false sinon
     */
    public boolean saveImageToFile(File file) {
        try {
            // Créer une image composite : image de base + canvas
            Image compositeImage = drawingService.createCompositeImage();

            // Déterminer le format à partir de l'extension
            String fileName = file.getName().toLowerCase();
            // On choisit PNG par défaut car c'est un format sans perte
            String format = "png";
            // Les deux extensions pointent vers le même format
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                format = "jpg";
            }

            // Gestion spéciale pour JPEG (pas de transparence)
            BufferedImage bufferedImage;
            if (format.equals("jpg")) {
                // Pour JPEG, créer une image avec fond blanc si nécessaire
                BufferedImage originalBuffered = SwingFXUtils.fromFXImage(compositeImage, null);
                
                // Créer une nouvelle image RGB (pas d'alpha)
                bufferedImage = new BufferedImage(
                        originalBuffered.getWidth(),
                        originalBuffered.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                
                Graphics2D g2d = bufferedImage.createGraphics();
                // Fond blanc seulement si l'image originale avait de la transparence
                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
                g2d.drawImage(originalBuffered, 0, 0, null);
                g2d.dispose();
            } else {
                // Pour PNG, conversion directe (supporte la transparence)
                bufferedImage = SwingFXUtils.fromFXImage(compositeImage, null);
            }

            // Sauvegarder l'image
            ImageIO.write(bufferedImage, format, file);
            
            // Mettre à jour le fichier source si c'était une sauvegarde directe
            if (file.equals(sourceFile) || sourceFile == null) {
                sourceFile = file;
            }
            
            // Marquer comme sauvegardé
            stateManager.markAsSaved();
            
            return true;
        } catch (IOException e) {
            showAlert("Save Error", "Unable to save image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crée un nouveau canvas de dessin vide.
     * 
     * @param defaultWidth La largeur par défaut du canvas
     * @param defaultHeight La hauteur par défaut du canvas
     */
    public void newCanvas(double defaultWidth, double defaultHeight) {
        // Créer un nouveau canvas vide
        currentImageProperty.set(null);
        imageModel.clear();
        sourceFile = null;
        
        // Redimensionner le canvas aux dimensions par défaut
        drawingService.getDrawingCanvas().setWidth(defaultWidth);
        drawingService.getDrawingCanvas().setHeight(defaultHeight);
        
        // Créer un canvas blanc par défaut
        drawingService.createDefaultCanvas();
        
        // Réinitialiser les flags de modification
        stateManager.markAsSaved();
    }
    
    /**
     * Retourne le fichier source actuel.
     * 
     * @return Le fichier source ou null
     */
    public File getSourceFile() {
        return sourceFile;
    }
    
    /**
     * Définit le fichier source.
     * 
     * @param file Le fichier source
     */
    public void setSourceFile(File file) {
        this.sourceFile = file;
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

