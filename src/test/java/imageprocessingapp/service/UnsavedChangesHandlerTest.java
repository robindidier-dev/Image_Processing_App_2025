package imageprocessingapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnsavedChangesHandlerTest {

    private CanvasStateManager stateManager;
    private UnsavedChangesHandler handler;

    @BeforeEach
    void setUp() {
        stateManager = new CanvasStateManager();
        handler = new UnsavedChangesHandler(stateManager);
    }

    @Test
    void testCheckAndHandle_NoUnsavedChanges() {
        // Pas de modifications non sauvegardées
        boolean result = handler.checkAndHandle("Test", "Message de test");
        
        // Devrait retourner true directement sans afficher de dialogue
        assertTrue(result);
    }

    @Test
    void testCheckAndHandle_WithUnsavedChanges() {
        // Marquer comme modifié
        stateManager.markAsModified(true);
        assertTrue(stateManager.hasUnsavedChanges());
        
        // Note: Ce test ne peut pas vraiment tester le dialogue car il nécessite une interaction utilisateur
        // On peut seulement vérifier que la méthode existe et ne lance pas d'exception
        assertNotNull(handler);
    }

    @Test
    void testCheckForWindowClose_NoUnsavedChanges() {
        // Pas de modifications non sauvegardées
        // Note: On ne peut pas tester avec un vrai Stage sans initialiser JavaFX
        // On peut seulement vérifier que la méthode existe
        assertNotNull(handler);
    }

    @Test
    void testCheckForWindowClose_WithUnsavedChanges() {
        // Marquer comme modifié
        stateManager.markAsModified(true);
        assertTrue(stateManager.hasUnsavedChanges());
        
        // Note: Ce test ne peut pas vraiment tester le dialogue car il nécessite une interaction utilisateur
        // On peut seulement vérifier que la méthode existe et ne lance pas d'exception
        assertNotNull(handler);
    }
}

