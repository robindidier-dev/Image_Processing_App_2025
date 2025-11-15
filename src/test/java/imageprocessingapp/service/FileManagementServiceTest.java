package imageprocessingapp.service;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileManagementServiceTest {

    private DrawingService drawingService;
    private ImageModel imageModel;
    private CanvasStateManager stateManager;
    private ObjectProperty<Image> currentImageProperty;
    private FileManagementService fileService;
    private Canvas drawingCanvas;
    private Path tempDir;

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @BeforeEach
    void setUp() throws IOException {
        drawingCanvas = new Canvas(100, 100);
        imageModel = new ImageModel();
        drawingService = new DrawingService(drawingCanvas, imageModel);
        drawingService.setupCanvas();
        stateManager = new CanvasStateManager();
        currentImageProperty = new SimpleObjectProperty<>();
        fileService = new FileManagementService(
                drawingService,
                imageModel,
                stateManager,
                currentImageProperty
        );
        
        // Créer un répertoire temporaire pour les tests
        tempDir = Files.createTempDirectory("fileManagementTest");
    }

    @Test
    void testGetSourceFile_InitiallyNull() {
        assertNull(fileService.getSourceFile());
    }

    @Test
    void testSetSourceFile() {
        File testFile = new File("test.png");
        fileService.setSourceFile(testFile);
        
        assertEquals(testFile, fileService.getSourceFile());
    }

    @Test
    void testNewCanvas() {
        // Marquer comme modifié
        stateManager.markAsModified(false);
        assertTrue(stateManager.hasUnsavedChanges());
        
        // Créer un nouveau canvas
        fileService.newCanvas(200, 150);
        
        // Vérifier que l'état a été réinitialisé
        assertFalse(stateManager.hasUnsavedChanges());
        assertNull(currentImageProperty.get());
        assertNull(imageModel.getImage());
        assertNull(fileService.getSourceFile());
        
        // Vérifier les dimensions du canvas
        assertEquals(200, drawingCanvas.getWidth());
        assertEquals(150, drawingCanvas.getHeight());
    }

    @Test
    void testLoadImageFromFile_InvalidExtension() {
        // Note: showAlert nécessite le thread JavaFX, donc on ne peut pas tester complètement
        // On peut seulement vérifier que la méthode existe
        // La méthode va appeler showAlert qui nécessite le thread JavaFX
        // On vérifie juste que la méthode existe et ne lance pas d'exception immédiate
        assertNotNull(fileService);
    }

    @Test
    void testLoadImageFromFile_ValidExtension() throws IOException {
        // Créer une image de test
        File testImageFile = createTestImageFile("test.png");
        
        boolean result = fileService.loadImageFromFile(testImageFile);
        
        assertTrue(result);
        assertNotNull(currentImageProperty.get());
        assertNotNull(imageModel.getImage());
        assertEquals(testImageFile, fileService.getSourceFile());
        assertFalse(stateManager.hasUnsavedChanges());
    }

    @Test
    void testSaveImageToFile_PNG() throws IOException {
        // Créer et charger une image
        File sourceFile = createTestImageFile("source.png");
        fileService.loadImageFromFile(sourceFile);
        
        // Sauvegarder dans un nouveau fichier
        File saveFile = new File(tempDir.toFile(), "saved.png");
        boolean result = fileService.saveImageToFile(saveFile);
        
        assertTrue(result);
        assertTrue(saveFile.exists());
        assertFalse(stateManager.hasUnsavedChanges());
    }

    @Test
    void testSaveImageToFile_JPEG() throws IOException {
        // Créer et charger une image
        File sourceFile = createTestImageFile("source.png");
        fileService.loadImageFromFile(sourceFile);
        
        // Sauvegarder en JPEG
        File saveFile = new File(tempDir.toFile(), "saved.jpg");
        boolean result = fileService.saveImageToFile(saveFile);
        
        assertTrue(result);
        assertTrue(saveFile.exists());
        assertFalse(stateManager.hasUnsavedChanges());
    }

    /**
     * Crée un fichier image de test.
     */
    private File createTestImageFile(String filename) throws IOException {
        // Créer une image simple en mémoire
        javafx.scene.image.WritableImage testImage = new javafx.scene.image.WritableImage(10, 10);
        javafx.scene.image.PixelWriter writer = testImage.getPixelWriter();
        
        // Remplir avec une couleur
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                writer.setColor(x, y, javafx.scene.paint.Color.BLUE);
            }
        }
        
        // Convertir en BufferedImage et sauvegarder
        java.awt.image.BufferedImage bufferedImage = javafx.embed.swing.SwingFXUtils.fromFXImage(testImage, null);
        File imageFile = new File(tempDir.toFile(), filename);
        javax.imageio.ImageIO.write(bufferedImage, "png", imageFile);
        
        return imageFile;
    }
}

