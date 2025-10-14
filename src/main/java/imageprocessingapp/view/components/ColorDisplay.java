package imageprocessingapp.view.components;

import imageprocessingapp.model.ColorUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * Widget pour afficher la couleur actuellement selectionnée.
 * Accompagnée d'un code hexadécimal pour identifier de façon unique
 * chaque couleur.
 * 'extends VBox' -> les composants internes sont l'un en dessous de l'autre
 */

public class ColorDisplay extends VBox {

    // Variables finales pour que les objets soient non réassignables (bonne pratique)
    // On n'écrit pas "private Color color" ici : à la place, on expose une propriété
    // et on peut bind color à la couleur choisie dans ColorPickerDialogController.

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>();
    private final Rectangle colorRectangle;
    private final Label hexColorCode;


    public ColorDisplay() {
        colorRectangle = new Rectangle();
        hexColorCode = new Label();

        // Ajout des éléments dans la HBox
        getChildren().addAll(colorRectangle, hexColorCode);

        colorRectangle.setWidth(85);
        colorRectangle.setHeight(50);

        // Listener pour que la couleur de fill du rectangle et le code hexadécimal
        // suivent la variable color
        color.addListener((obs, oldColor, newColor) -> updateColorDisplay(newColor));
    }

    private final void updateColorDisplay(Color c) {
        colorRectangle.setFill(c);
        hexColorCode.setText(ColorUtils.colorToHex(c));
    }

    // Bonne pratique : avoir à la fois un getter/setter
    // ET une propriété exposée pour le binding

    public final void setColor(Color c) {
        color.set(c);
    }

    public final Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }



}
