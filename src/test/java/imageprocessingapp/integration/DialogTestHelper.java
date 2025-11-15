package imageprocessingapp.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

/**
 * Classe utilitaire pour faciliter les tests d'intégration des dialogues.
 * 
 * Fournit des méthodes helper pour charger et configurer les dialogues FXML
 * de manière cohérente dans les tests.
 */
public class DialogTestHelper {

    /**
     * Charge un dialogue FXML et retourne le Parent et le Stage configurés.
     * 
     * @param fxmlPath Le chemin vers le fichier FXML
     * @return Un objet contenant le Parent et le Stage
     * @throws Exception Si le chargement échoue
     */
    public static DialogComponents loadDialog(String fxmlPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(DialogTestHelper.class.getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        return new DialogComponents(root, stage, loader.getController());
    }

    /**
     * Trouve un bouton dans le dialogue par son ID.
     * 
     * @param root Le Parent du dialogue
     * @param buttonId L'ID du bouton
     * @return Le bouton trouvé
     */
    public static Button findButton(Parent root, String buttonId) {
        Button button = (Button) root.lookup("#" + buttonId);
        if (button == null) {
            throw new AssertionError("Button with id '" + buttonId + "' should be present in the dialog");
        }
        return button;
    }

    /**
     * Trouve un slider dans le dialogue par son ID.
     * 
     * @param root Le Parent du dialogue
     * @param sliderId L'ID du slider
     * @return Le slider trouvé
     */
    public static Slider findSlider(Parent root, String sliderId) {
        Slider slider = (Slider) root.lookup("#" + sliderId);
        if (slider == null) {
            throw new AssertionError("Slider with id '" + sliderId + "' should be present in the dialog");
        }
        return slider;
    }

    /**
     * Classe interne pour contenir les composants d'un dialogue chargé.
     */
    public static class DialogComponents {
        public final Parent root;
        public final Stage stage;
        public final Object controller;

        public DialogComponents(Parent root, Stage stage, Object controller) {
            this.root = root;
            this.stage = stage;
            this.controller = controller;
        }
    }
}

