package imageprocessingapp.service;

/**
 * Gère l'état du canvas (modifications, sauvegarde).
 * 
 * Cette classe encapsule la logique de suivi des modifications du canvas,
 * permettant de distinguer entre les modifications du canvas par défaut
 * et celles du canvas overlay.
 */
public class CanvasStateManager {
    
    /**
     * Indique si le canvas a été modifié depuis la dernière sauvegarde.
     */
    private boolean canvasModified = false;
    
    /**
     * Indique si l'image par défaut (canvas blanc) a été modifiée.
     */
    private boolean defaultCanvasModified = false;
    
    /**
     * Marque le canvas comme modifié.
     * 
     * @param hasCurrentImage true si une image est actuellement chargée, false sinon
     */
    public void markAsModified(boolean hasCurrentImage) {
        if (hasCurrentImage) {
            // Si une image est chargée, on modifie le canvas superposé
            canvasModified = true;
        } else {
            // Si aucune image n'est chargée, on modifie le canvas par défaut
            defaultCanvasModified = true;
        }
    }
    
    /**
     * Marque le canvas comme non modifié (après sauvegarde).
     */
    public void markAsSaved() {
        canvasModified = false;
        defaultCanvasModified = false;
    }
    
    /**
     * Vérifie si le canvas a été modifié.
     * 
     * @return true si le canvas a été modifié, false sinon
     */
    public boolean isCanvasModified() {
        return canvasModified;
    }
    
    /**
     * Vérifie si le canvas par défaut a été modifié.
     * 
     * @return true si le canvas par défaut a été modifié, false sinon
     */
    public boolean isDefaultCanvasModified() {
        return defaultCanvasModified;
    }
    
    /**
     * Vérifie s'il y a des modifications non sauvegardées.
     * 
     * @return true s'il y a des modifications non sauvegardées, false sinon
     */
    public boolean hasUnsavedChanges() {
        return canvasModified || defaultCanvasModified;
    }
    
    /**
     * Réinitialise l'état du canvas.
     */
    public void reset() {
        canvasModified = false;
        defaultCanvasModified = false;
    }
}

