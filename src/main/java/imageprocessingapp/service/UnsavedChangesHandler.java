package imageprocessingapp.service;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Gère la détection et la gestion des modifications non sauvegardées.
 * 
 * Cette classe encapsule la logique d'affichage des dialogues de confirmation
 * lorsque l'utilisateur tente d'effectuer une action qui pourrait perdre
 * des modifications non sauvegardées.
 */
public class UnsavedChangesHandler {
    
    private final CanvasStateManager stateManager;
    
    /**
     * Constructeur.
     * 
     * @param stateManager Le gestionnaire d'état du canvas
     */
    public UnsavedChangesHandler(CanvasStateManager stateManager) {
        this.stateManager = stateManager;
    }
    
    /**
     * Vérifie s'il y a des modifications non sauvegardées et affiche un dialogue si nécessaire.
     * 
     * @param actionTitle Le titre de l'action en cours (ex: "ouvrir une nouvelle image")
     * @param actionMessage Le message décrivant l'action
     * @param parentStage La fenêtre parente pour le dialogue
     * @return true si l'action peut continuer, false si elle doit être annulée
     */
    public boolean checkAndHandle(String actionTitle, String actionMessage, Stage parentStage) {
        if (!stateManager.hasUnsavedChanges()) {
            return true;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Modifications non sauvegardées");
        alert.setHeaderText("Vous avez des modifications non sauvegardées.");
        alert.setContentText(actionMessage);
        
        if (parentStage != null) {
            alert.initOwner(parentStage);
        }
        
        ButtonType saveButton = new ButtonType("Sauvegarder");
        ButtonType discardButton = new ButtonType("Ignorer");
        ButtonType cancelButton = new ButtonType("Annuler");
        
        alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);
        
        ButtonType result = alert.showAndWait().orElse(cancelButton);
        
        if (result == saveButton) {
            // L'appelant doit sauvegarder puis continuer
            return true; // Indique qu'il faut sauvegarder puis continuer
        } else if (result == discardButton) {
            // L'utilisateur veut ignorer les modifications - réinitialiser l'état
            stateManager.reset();
            return true; // L'action peut continuer
        } else {
            // L'utilisateur a annulé
            return false; // L'action doit être annulée
        }
    }
    
    /**
     * Vérifie s'il y a des modifications non sauvegardées et affiche un dialogue si nécessaire.
     * Version simplifiée sans stage parent.
     * 
     * @param actionTitle Le titre de l'action en cours
     * @param actionMessage Le message décrivant l'action
     * @return true si l'action peut continuer, false si elle doit être annulée
     */
    public boolean checkAndHandle(String actionTitle, String actionMessage) {
        return checkAndHandle(actionTitle, actionMessage, null);
    }
    
    /**
     * Vérifie s'il y a des modifications non sauvegardées pour la fermeture de fenêtre.
     * 
     * @param parentStage La fenêtre parente
     * @return true si la fermeture peut continuer, false si elle doit être annulée
     */
    public boolean checkForWindowClose(Stage parentStage) {
        if (!stateManager.hasUnsavedChanges()) {
            return true;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Modifications non sauvegardées");
        alert.setHeaderText("Vous avez des modifications non sauvegardées.");
        alert.setContentText("Voulez-vous sauvegarder avant de fermer ?");
        
        if (parentStage != null) {
            alert.initOwner(parentStage);
        }
        
        ButtonType saveButton = new ButtonType("Sauvegarder");
        ButtonType discardButton = new ButtonType("Ignorer");
        ButtonType cancelButton = new ButtonType("Annuler");
        
        alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);
        
        ButtonType result = alert.showAndWait().orElse(cancelButton);
        
        if (result == saveButton) {
            // L'appelant doit sauvegarder puis continuer
            return true; // Indique qu'il faut sauvegarder puis continuer
        } else if (result == discardButton) {
            // L'utilisateur veut ignorer les modifications
            return true; // La fermeture peut continuer
        } else {
            // L'utilisateur a annulé
            return false; // La fermeture doit être annulée
        }
    }
}

