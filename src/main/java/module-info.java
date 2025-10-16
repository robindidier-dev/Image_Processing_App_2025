module imageprocessingapp {
    // Dépendances du module
    requires javafx.controls; // Pour les contrôles UI JavaFX (boutons, menus etc)
    requires javafx.fxml; // Pour charger les fichiers FXML
    requires javafx.graphics; // Pour le rendu graphique
    requires java.desktop; // Pour les fonctionnalités desktop (AWT/Swing)  
    requires java.rmi; // Pour Java RMI (Remote Method Invocation)
    requires javafx.swing;
    requires javafx.base; // Pour l'interopérabilité JavaFX-Swing
    requires jdk.compiler;

    // Packages exportés et ouverts
    exports imageprocessingapp; // Package principal accessible aux autres modules
    exports imageprocessingapp.model.tools; // Package des outils accessible aux autres modules
    exports imageprocessingapp.view.components;
    opens imageprocessingapp.view to javafx.fxml; // Ouvert à javafx.fxml pour l'injection de dépendances
    opens imageprocessingapp.controller to javafx.fxml; // Ouvert à javafx.fxml pour l'injection de dépendances
}