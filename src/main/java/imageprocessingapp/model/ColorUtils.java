package imageprocessingapp.model;
import javafx.scene.paint.Color;

/**
 * Classe utilitaire pour la gestion des couleurs dans l'application.
 * Fournit des méthodes de conversion entre différents espaces colorimétriques
 * et des opérations sur les couleurs.
 * Par conséquent, celle-ci sera 'final' et son constructeur sera privé.
 * Toutes les méthodes seront par ailleurs statiques.
 */

public final class ColorUtils {

    private ColorUtils() {
        // Constructeur privé pour qu'il ne soit pas appelé
        // Constructeur non obligatoire, car créé par Java automatiquement

        throw new AssertionError("ColorUtils: not callable method");
        // Assertion : bonne pratique
    }

    public static String colorToHex(Color c) {
        // Il n'existe pas de méthode native pour une conversion vers hexadécimal.
        // On la réimplémente manuellement.

        // Ici on cast Math.round avec (int) car c.getRed() est un double entre 0 et 1.
        // Le type renvoyé par Math.round est dans ce cas un long.
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);

        return "#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);

    }

    public static Color hextoColor(String s) {
        // On utilise ma méthode existante pour convertir un code hexadécimal en Color.
        // Structure type try catch au cas où l'entrée n'est pas un code hexadécimal.
        try {
            return Color.web(s);
        } catch (IllegalArgumentException e) {
            return Color.BLACK;
        }
    }
}

