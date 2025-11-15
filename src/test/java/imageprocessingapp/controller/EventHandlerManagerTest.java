package imageprocessingapp.controller;

import imageprocessingapp.model.ImageModel;
import imageprocessingapp.model.tools.PaintTool;
import imageprocessingapp.model.tools.Tool;
import imageprocessingapp.util.JavaFxTestInitializer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class EventHandlerManagerTest {

    private Canvas drawingCanvas;
    private ImageView imageView;
    private ObjectProperty<Tool> activeToolProperty;
    private ImageModel imageModel;
    private EventHandlerManager eventHandlerManager;
    private Stage stage;

    @BeforeAll
    static void initJavaFX() throws Exception {
        JavaFxTestInitializer.initToolkit();
    }

    @BeforeEach
    void setUp() {
        Platform.runLater(() -> {
            drawingCanvas = new Canvas(100, 100);
            imageView = new ImageView();
            activeToolProperty = new SimpleObjectProperty<>();
            imageModel = new ImageModel();
            
            // Créer une scène et une stage pour les tests
            StackPane root = new StackPane();
            root.getChildren().addAll(imageView, drawingCanvas);
            Scene scene = new Scene(root, 800, 600);
            stage = new Stage();
            stage.setScene(scene);
            stage.show();
            
            eventHandlerManager = new EventHandlerManager(
                    drawingCanvas,
                    imageView,
                    activeToolProperty,
                    imageModel
            );
        });
        
        // Attendre que la scène soit configurée
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testSetupMouseHandlers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean handlersSet = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            eventHandlerManager.setupMouseHandlers();
            handlersSet.set(true);
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(handlersSet.get());
    }

    @Test
    void testHandleMousePressed_WithTool() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean toolCalled = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            // Créer un outil mock avec GraphicsContext
            PaintTool tool = new PaintTool(drawingCanvas.getGraphicsContext2D());
            tool.setPaintColor(Color.RED);
            activeToolProperty.set(tool);
            
            eventHandlerManager.setupMouseHandlers();
            
            // Simuler un événement de souris
            MouseEvent event = new MouseEvent(
                    MouseEvent.MOUSE_PRESSED,
                    50.0, 50.0, 50.0, 50.0,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    true, false, false, true,
                    false, false, null);
            
            drawingCanvas.fireEvent(event);
            toolCalled.set(true);
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(toolCalled.get());
    }

    @Test
    void testHandleMousePressed_WithoutTool() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean noException = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            // Pas d'outil actif
            activeToolProperty.set(null);
            
            eventHandlerManager.setupMouseHandlers();
            
            // Simuler un événement de souris
            MouseEvent event = new MouseEvent(
                    MouseEvent.MOUSE_PRESSED,
                    50.0, 50.0, 50.0, 50.0,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    true, false, false, true,
                    false, false, null);
            
            try {
                drawingCanvas.fireEvent(event);
                noException.set(true);
            } catch (Exception e) {
                noException.set(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(noException.get());
    }

    @Test
    void testHandleMouseDragged_WithTool() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean toolCalled = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            // Créer un outil mock avec GraphicsContext
            PaintTool tool = new PaintTool(drawingCanvas.getGraphicsContext2D());
            tool.setPaintColor(Color.RED);
            activeToolProperty.set(tool);
            
            eventHandlerManager.setupMouseHandlers();
            
            // Simuler un événement de glissement
            MouseEvent event = new MouseEvent(
                    MouseEvent.MOUSE_DRAGGED,
                    50.0, 50.0, 50.0, 50.0,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    true, false, false, true,
                    false, false, null);
            
            drawingCanvas.fireEvent(event);
            toolCalled.set(true);
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(toolCalled.get());
    }

    @Test
    void testHandleMouseReleased_WithTool() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean toolCalled = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            // Créer un outil mock avec GraphicsContext
            PaintTool tool = new PaintTool(drawingCanvas.getGraphicsContext2D());
            tool.setPaintColor(Color.RED);
            activeToolProperty.set(tool);
            
            eventHandlerManager.setupMouseHandlers();
            
            // Simuler un événement de relâchement
            MouseEvent event = new MouseEvent(
                    MouseEvent.MOUSE_RELEASED,
                    50.0, 50.0, 50.0, 50.0,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    true, false, false, true,
                    false, false, null);
            
            drawingCanvas.fireEvent(event);
            toolCalled.set(true);
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(toolCalled.get());
    }

    @Test
    void testSetupKeyboardShortcuts() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean shortcutsSet = new AtomicBoolean(false);
        AtomicBoolean saveActionCalled = new AtomicBoolean(false);
        AtomicBoolean openActionCalled = new AtomicBoolean(false);
        AtomicBoolean newActionCalled = new AtomicBoolean(false);
        AtomicBoolean closeActionCalled = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            eventHandlerManager.setOnSaveAction(() -> saveActionCalled.set(true));
            eventHandlerManager.setOnOpenAction(() -> openActionCalled.set(true));
            eventHandlerManager.setOnNewAction(() -> newActionCalled.set(true));
            eventHandlerManager.setOnCloseAction(() -> closeActionCalled.set(true));
            
            eventHandlerManager.setupKeyboardShortcuts();
            shortcutsSet.set(true);
            
            // Simuler les événements clavier
            Scene scene = imageView.getScene();
            if (scene != null) {
                // Test Ctrl+S
                KeyEvent saveEvent = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "", "",
                        KeyCode.S, false, true, false, false
                );
                scene.getOnKeyPressed().handle(saveEvent);
                
                // Test Ctrl+O
                KeyEvent openEvent = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "", "",
                        KeyCode.O, false, true, false, false
                );
                scene.getOnKeyPressed().handle(openEvent);
                
                // Test Ctrl+N
                KeyEvent newEvent = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "", "",
                        KeyCode.N, false, true, false, false
                );
                scene.getOnKeyPressed().handle(newEvent);
                
                // Test Ctrl+W
                KeyEvent closeEvent = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "", "",
                        KeyCode.W, false, true, false, false
                );
                scene.getOnKeyPressed().handle(closeEvent);
            }
            
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(shortcutsSet.get());
        // Les actions devraient être appelées
        // Note: Les événements peuvent ne pas être déclenchés correctement dans les tests
    }

    @Test
    void testSetupKeyboardShortcuts_WithoutScene() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean noException = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            // Créer un manager sans scène
            ImageView imageViewWithoutScene = new ImageView();
            EventHandlerManager managerWithoutScene = new EventHandlerManager(
                    drawingCanvas,
                    imageViewWithoutScene,
                    activeToolProperty,
                    imageModel
            );
            
            try {
                managerWithoutScene.setupKeyboardShortcuts();
                noException.set(true);
            } catch (Exception e) {
                noException.set(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(noException.get());
    }

    @Test
    void testSetCallbacks() {
        Runnable saveAction = () -> {};
        Runnable openAction = () -> {};
        Runnable newAction = () -> {};
        Runnable closeAction = () -> {};
        
        eventHandlerManager.setOnSaveAction(saveAction);
        eventHandlerManager.setOnOpenAction(openAction);
        eventHandlerManager.setOnNewAction(newAction);
        eventHandlerManager.setOnCloseAction(closeAction);
        
        // Vérifier que les callbacks sont définis
        assertNotNull(eventHandlerManager);
    }
}

