package imageprocessingapp.controller;

import javafx.scene.layout.Region;

/**
 * Contrôleur gérant le zoom sur la vue principale et les déplacements.
 */
public class ZoomController {

    // Échelle de l'affichage
    private double scale = 1;

    // StackPane contenant imageView + canvas
    private final Region container;

    // Coordonnées pour le déplacement
    private double lastX;
    private double lastY;

    // Permet de vérifier si un outil est sélectionné avant de translater
    MainController mainController;

    // Instanciation de mainController
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    public ZoomController(Region container) {
        this.container = container;
    }


    // Méthode qui active le zoom centré sur le conteneur
    public void setupZoom() {
        container.setOnScroll(event -> {
            double scaleChange;

            if (event.getDeltaY() > 0) {
                scaleChange = 1.1;
            } else {
                scaleChange = 0.9;
            }

            // Zoom maximum et minimum
            double newScale = scale * scaleChange;

            if (newScale < 0.1 || newScale > 10) {
                return;
            }

            scale *= scaleChange;
            container.setScaleX(scale);
            container.setScaleY(scale);
        });
    }

    // Méthode qui active la translation
    public void setupTranslate() {
        container.setOnMousePressed(event -> {
            // Vérifie si un outil est actif
            if (mainController.activeToolProperty().get() != null) {
              return;
            }

            // Position initiale du clic
            lastX = event.getSceneX();
            lastY = event.getSceneY();
        });

        container.setOnMouseDragged(event -> {
            // Vérifie si un outil est actif
            if (mainController == null || mainController.activeToolProperty().get() != null) {
                return;
            }

            // Calcule le déplacement
            double deltaX = event.getSceneX() - lastX;
            double deltaY = event.getSceneY() - lastY;

            // Applique la translation
            container.setTranslateX(container.getTranslateX() + deltaX);
            container.setTranslateY(container.getTranslateY() + deltaY);

            lastX = event.getSceneX();
            lastY = event.getSceneY();
        });
    }

    // Réinitialise le zoom et la position
    public void resetView() {
        scale = 1;
        container.setScaleX(1);
        container.setScaleY(1);
        container.setTranslateX(0);
        container.setTranslateY(0);

    }

}