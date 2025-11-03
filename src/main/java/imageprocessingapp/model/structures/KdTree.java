package imageprocessingapp.model.structures;

import java.util.Objects;
import java.util.Optional;

public class KdTree {
    private static final int K = 2;

    /**
     * Classe interne pour les nœuds de l'arbre KD.
     */
    private static final class Node {
        final Point2D point;
        final int depth;
        Node left;
        Node right;

        /**
         * Constructeur pour un nœud de l'arbre KD.
         * @param point Le point associé à ce nœud
         * @param depth La profondeur de ce nœud dans l'arbre
         */
        Node(Point2D point, int depth) {
            this.point = point;
            this.depth = depth;
        }
    }

    // Attributes
    private Node root;
    private int size;

    // Getters
    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    /**
     * Insère un point dans l'arbre KD à partir de la racine.
     * Méthode simple public pour les appels externes.
     * @param p Le point à insérer
     */
    public void insert(Point2D p) {
        Objects.requireNonNull(p, "point must not be null");
        root = insert(root, p, 0);
    }

    /**
     * Insère un point dans l'arbre KD avec récursivité.
     * Méthode interne pour insérer un point dans l'arbre KD.
     * @param node Le nœud parent
     * @param p Le point à insérer
     * @param depth La profondeur du nœud
     * @return Le nœud inséré
     */
    private Node insert(Node node, Point2D p, int depth) {
        // Cas de base: on crée un nouveau nœud
        if (node == null) {
            size++;
            return new Node(p, depth);
        }
        // Eviter les doublons exacts: on ignore l'insertion si point identique
        if (node.point.equals(p)) {
            return node;
        }
        // On choisit l'axe de séparation en fonction de la profondeur
        int axis = depth % K; // 0 -> x, 1 -> y
        double pivot = (axis == 0) ? node.point.x() : node.point.y();
        double value = (axis == 0) ? p.x() : p.y();

        // On insère le point dans le sous-arbre correspondant
        if (value < pivot) {
            node.left = insert(node.left, p, depth + 1);
        } else {
            node.right = insert(node.right, p, depth + 1);
        }
        return node;
    }

    /**
     * Recherche le point le plus proche d'un point cible donné dans l'arbre KD.
     * Cette méthode permet de retrouver, parmi tous les points stockés dans l'arbre,
     * le point dont la distance euclidienne par rapport à <code>target</code> est la plus faible.
     * @param target Le point pour lequel on veut trouver le voisin le plus proche dans l'arbre.
     * @return Un Optional contenant le point le plus proche si l'arbre n'est pas vide.
     * @throws NullPointerException si le point cible passé en paramètre est nul.
     */
    public Optional<Point2D> findNearest(Point2D target) {
        Objects.requireNonNull(target, "target must not be null");
        if (root == null) return Optional.empty();
        // Lancement de la recherche à partir de la racine,
        // avec comme "meilleur point courant" le point de la racine (puisqu'on vient juste de commencer)
        return Optional.of(
            searchNearest(root, target, root.point, root.point.distanceSquared(target))
        );
    }

    /**
     * Recherche le point le plus proche dans l'arbre KD.
     * @param node Le nœud courant
     * @param target Le point cible
     * @param best Le meilleur point trouvé jusqu'à présent
     * @param bestDistSq La distance au carré du meilleur point trouvé jusqu'à présent
     * @return Le point le plus proche
     */
    private Point2D searchNearest(Node node, Point2D target, Point2D best, double bestDistSq) {
        if (node == null) return best;

        double d2 = node.point.distanceSquared(target);
        Point2D currentBest = best;
        double currentBestDistSq = bestDistSq;

        // On met à jour le meilleur point trouvé jusqu'à présent
        if (d2 < currentBestDistSq) {
            currentBest = node.point;
            currentBestDistSq = d2;
        }

        // On choisit l'axe de séparation en fonction de la profondeur
        int axis = node.depth % K;
        double targetVal = (axis == 0) ? target.x() : target.y();
        double nodeVal = (axis == 0) ? node.point.x() : node.point.y();

        Node near = (targetVal < nodeVal) ? node.left : node.right;
        Node far  = (targetVal < nodeVal) ? node.right : node.left;

        // Explorer la branche la plus proche en premier
        currentBest = searchNearest(near, target, currentBest, currentBestDistSq);
        currentBestDistSq = currentBest.distanceSquared(target);

        // Critère de "pruning": si l'hyperplan est plus proche que le meilleur, explorer l'autre branche
        double planeDist = targetVal - nodeVal;
        double planeDistSq = planeDist * planeDist;
        if (planeDistSq < currentBestDistSq) {
            currentBest = searchNearest(far, target, currentBest, currentBestDistSq);
        }

        return currentBest;
    }
}