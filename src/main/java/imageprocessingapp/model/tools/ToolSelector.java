package imageprocessingapp.model.tools;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class ToolSelector extends VBox {

    private VBox container;
    private ToggleGroup groupeOutils; // groupe de ToggleButtons pour que seul un ToggleButton soit actif à la fois

    public ToolSelector(VBox container) {
        this.container = container; // le conteneur passé en paramètre sera la VBox du MainView.fxml
    }


    // Crée un ToggleGroup, qui va permettre de gérer la sélection unique des boutons outils
    public void createToggleGroup() {
        groupeOutils = new ToggleGroup();
    }


    // Associe un ToggleButton au groupe, en créant le groupe si besoin
    // Cela garantit qu'un seul bouton du groupe est sélectionné simultanément
    public void setupToggleGroup(ToggleButton toggleButton) {
        if (groupeOutils == null) {
            createToggleGroup();
        }
        toggleButton.setToggleGroup(groupeOutils);
    }
}
