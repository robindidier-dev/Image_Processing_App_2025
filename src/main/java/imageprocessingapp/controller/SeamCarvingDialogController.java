package imageprocessingapp.controller;

import imageprocessingapp.service.filters.SeamCarvingService;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Contrôleur du dialogue Seam Carving.
 */
public class SeamCarvingDialogController {

    @FXML
    private Spinner<Integer> widthSpinner;

    @FXML
    private Spinner<Integer> heightSpinner;

    private SeamCarvingService seamCarvingService;

    public void initialize() {
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));
    }

    public void setSeamCarvingService(SeamCarvingService seamCarvingService) {
        this.seamCarvingService = seamCarvingService;
    }

    @FXML
    private void onApply() {
        // TODO: déclencher le redimensionnement via seamCarvingService
    }

    @FXML
    private void onPreview() {
        // TODO: afficher une prévisualisation de la prochaine couture
    }
}

