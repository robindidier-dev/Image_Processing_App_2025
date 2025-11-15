package imageprocessingapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CanvasStateManagerTest {

    private CanvasStateManager stateManager;

    @BeforeEach
    void setUp() {
        stateManager = new CanvasStateManager();
    }

    @Test
    void testInitialState() {
        assertFalse(stateManager.isCanvasModified());
        assertFalse(stateManager.isDefaultCanvasModified());
        assertFalse(stateManager.hasUnsavedChanges());
    }

    @Test
    void testMarkAsModified_WithImage() {
        stateManager.markAsModified(true);
        
        assertTrue(stateManager.isCanvasModified());
        assertFalse(stateManager.isDefaultCanvasModified());
        assertTrue(stateManager.hasUnsavedChanges());
    }

    @Test
    void testMarkAsModified_WithoutImage() {
        stateManager.markAsModified(false);
        
        assertFalse(stateManager.isCanvasModified());
        assertTrue(stateManager.isDefaultCanvasModified());
        assertTrue(stateManager.hasUnsavedChanges());
    }

    @Test
    void testMarkAsSaved() {
        stateManager.markAsModified(true);
        stateManager.markAsModified(false);
        
        assertTrue(stateManager.hasUnsavedChanges());
        
        stateManager.markAsSaved();
        
        assertFalse(stateManager.isCanvasModified());
        assertFalse(stateManager.isDefaultCanvasModified());
        assertFalse(stateManager.hasUnsavedChanges());
    }

    @Test
    void testReset() {
        stateManager.markAsModified(true);
        stateManager.markAsModified(false);
        
        assertTrue(stateManager.hasUnsavedChanges());
        
        stateManager.reset();
        
        assertFalse(stateManager.isCanvasModified());
        assertFalse(stateManager.isDefaultCanvasModified());
        assertFalse(stateManager.hasUnsavedChanges());
    }

    @Test
    void testMultipleModifications() {
        stateManager.markAsModified(true);
        stateManager.markAsModified(true);
        
        assertTrue(stateManager.isCanvasModified());
        assertTrue(stateManager.hasUnsavedChanges());
        
        stateManager.markAsSaved();
        
        assertFalse(stateManager.isCanvasModified());
        assertFalse(stateManager.hasUnsavedChanges());
    }

    @Test
    void testHasUnsavedChanges_CanvasModifiedOnly() {
        stateManager.markAsModified(true);
        
        assertTrue(stateManager.hasUnsavedChanges());
        assertTrue(stateManager.isCanvasModified());
        assertFalse(stateManager.isDefaultCanvasModified());
    }

    @Test
    void testHasUnsavedChanges_DefaultCanvasModifiedOnly() {
        stateManager.markAsModified(false);
        
        assertTrue(stateManager.hasUnsavedChanges());
        assertFalse(stateManager.isCanvasModified());
        assertTrue(stateManager.isDefaultCanvasModified());
    }

    @Test
    void testHasUnsavedChanges_BothModified() {
        stateManager.markAsModified(true);
        stateManager.markAsModified(false);
        
        assertTrue(stateManager.hasUnsavedChanges());
        assertTrue(stateManager.isCanvasModified());
        assertTrue(stateManager.isDefaultCanvasModified());
    }

    @Test
    void testReset_AfterModifications() {
        stateManager.markAsModified(true);
        stateManager.markAsModified(false);
        
        assertTrue(stateManager.hasUnsavedChanges());
        
        stateManager.reset();
        
        assertFalse(stateManager.hasUnsavedChanges());
        assertFalse(stateManager.isCanvasModified());
        assertFalse(stateManager.isDefaultCanvasModified());
    }

    @Test
    void testMarkAsSaved_AfterReset() {
        stateManager.markAsModified(true);
        stateManager.reset();
        stateManager.markAsSaved();
        
        // Ne devrait pas changer l'état après reset
        assertFalse(stateManager.hasUnsavedChanges());
    }

    @Test
    void testMarkAsModified_WithImage_ThenWithout() {
        stateManager.markAsModified(true);
        assertTrue(stateManager.isCanvasModified());
        assertFalse(stateManager.isDefaultCanvasModified());
        
        stateManager.markAsModified(false);
        assertTrue(stateManager.isCanvasModified());
        assertTrue(stateManager.isDefaultCanvasModified());
    }
}

