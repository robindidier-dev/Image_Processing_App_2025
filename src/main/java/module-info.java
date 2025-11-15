module imageprocessingapp {
    // Dépendances du module
    requires transitive javafx.controls; // Pour les contrôles UI JavaFX (boutons, menus etc)
    requires javafx.fxml; // Pour charger les fichiers FXML
    requires transitive javafx.graphics; // Pour le rendu graphique 
    requires java.desktop; // Pour les fonctionnalités desktop (AWT/Swing)  
    requires java.rmi; // Pour Java RMI (Remote Method Invocation)
    requires javafx.swing;
    requires javafx.base;
    requires jdk.xml.dom; // Pour l'interopérabilité JavaFX-Swing

    // Packages exportés et ouverts
    exports imageprocessingapp; // Package principal accessible aux autres modules
    exports imageprocessingapp.model; // Package du modèle accessible aux autres modules
    exports imageprocessingapp.model.tools; // Package des outils accessible aux autres modules
    exports imageprocessingapp.model.filters; // Package des filtres (mosaïque, seam carving)
    exports imageprocessingapp.model.structures; // Package des structures de données (Point2D, KdTree, etc.)
    exports imageprocessingapp.service.filters; // Couche service partagée
    exports imageprocessingapp.view.components; // Package des composants d'interface
    exports imageprocessingapp.controller; // Package des contrôleurs
    
    // Ouvert à javafx.fxml pour l'injection de dépendances
    opens imageprocessingapp.controller to javafx.fxml;
}