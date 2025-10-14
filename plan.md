# Plan de développement - Image Processing App

## Structure du projet

```
src/main/java/imageprocessingapp/
├── MainApp.java                  # Point d'entrée de l'application
├── model/                        # Logique métier et données
│  ├── ImageModel.java            # Modèle principal de l'image
│  ├── ColorUtils.java            # Utilitaires pour les couleurs
│  ├── tools/                     # Outils de dessin
│  │  └── Tool.java               # Interface des outils
│  ├── filters/                   # Filtres/effets (mosaïque, seam carving)
│  └── structures/                # Structures de données (KdTree)
├── view/                         # Composants d'interface utilisateur
│  └── components/                # Widgets réutilisables
└── controller/                   # Logique de contrôle
   ├── MainController.java        # Contrôleur principal
   └── tool/                      # Contrôleurs des outils

src/main/resources/imageprocessingapp/
├── view/                      
│  └── MainView.fxml              # Interface principale (FXML)
└── dialogs/                      # Fenêtres de dialogue

src/test/java/imageprocessingapp/
├── model/                        # Tests unitaires du model
└── integration/                  # Tests d'intégration
```

## Répartition des tâches

### **Etape 1 : Infrastructure de base**

#### Paul-Antoine - View principale

**Branch:** `paulantoine/main-view`

- Créer `MainView.fxml` avec BorderPane (Menu top, Toolbar left, ImageView center)
- Créer `MainController.java` avec imageProperty, selectedColorProperty
- Implémenter Menu File (Open/Save) avec FileChooser
- Connecter ImageView avec ImageModel

#### Robin - Système de couleurs (Model + View)

**Branch:** `robin/color-system`

- Créer widget `ColorDisplay` (HBox avec Rectangle coloré + Label)
- Créer `ColorPickerDialog` avec Sliders RGB et visualisation
- Implémenter binding entre sliders et selectedColorProperty
- Tests pour ColorUtils

#### Adrien - Système d'outils (Model + Controller)

**Branch:** `adrien/tool-system`

- Créer interface `Tool` dans `model/tools/` avec methods `onMousePressed`, `onMouseDragged`
- Implémenter `PaintTool` (dessine cercles) et `PickerTool` (lit couleur pixel)
- Créer `ToolSelector` widget (VBox avec ToggleButtons)
- Gérer activation/désactivation des outils dans MainController

**Merge Etape 1:** Chacun fait une PR, les deux autres reviewent, puis merge dans l'ordre (main-view → color-system → tool-system)

---

### **Etape 2 : Mosaic Effect**

#### Paul-Antoine - Model KdTree

**Branch:** `paulantoine/kdtree-model`

- Créer `Point2D` dans `model/structures/` (x, y, distance)
- Implémenter `KdTree` avec insertion et `findNearest`
- Suivre pseudo-code slides (récursif)
- Tests unitaires KdTree (insertion, recherche, cas limites)

#### Robin - Filter Mosaic

**Branch:** `robin/mosaic-filter`

- Créer `MosaicFilter` dans `model/filters/`
- Générer seeds aléatoires (ou grille régulière)
- Pour chaque pixel: utiliser KdTree.findNearest
- Calculer couleur moyenne par cellule Voronoi
- Tests MosaicFilter avec petite image

#### Adrien - UI Mosaic

**Branch:** `adrien/mosaic-ui`

- Créer `MosaicDialog.fxml` avec Spinner (nombre seeds), ChoiceBox (distribution)
- Controller avec application du filtre et preview
- Ajouter Menu > Filters > Mosaic Effect
- Intégration avec MainController

**Merge Etape 2:** kdtree-model → mosaic-filter → mosaic-ui

---

### **Etape 3 : Seam Carving**

Feature complète répartie par couche MVC.

#### Robin - Model Energy

**Branch:** `robin/energy-calculator`

- Créer `EnergyCalculator` dans `model/filters/`
- Implémenter calcul gradient (différence pixels voisins)
- Méthode `computeEnergyMap(ImageModel)` retourne double[][]
- Tests avec images simples (bords détectés)

#### Paul-Antoine - Algorithm SeamCarver

**Branch:** `paulantoine/seam-algorithm`

- Créer `SeamCarver` dans `model/filters/`
- Méthode `computeCumulativeEnergy` (programmation dynamique)
- Méthode `findSeam` (backtracking depuis min dernière ligne)
- Méthode `removeSeam` (décalage pixels)
- Tests pour vertical ET horizontal

#### Adrien - UI Seam Carving

**Branch:** `adrien/seam-ui`

- Créer `SeamCarvingDialog.fxml` avec TextField (largeur/hauteur)
- Controller avec boucle de suppression de seams
- Ajouter Menu > Edit > Resize with Seam Carving
- Option: bouton preview pour visualiser prochaine couture

**Merge Etape 3:** energy-calculator → seam-algorithm → seam-ui

---

### **Etape 4 : Finitions & Tests**

#### Paul-Antoine

#### Robin

#### Adrien

---

### To-dos

- [X] Supprimer le package teamteacher et mettre à jour module-info.java
- [X] Créer MainApp.java, MainController.java et MainView.fxml avec menu/toolbar/imageview
- [x] Implémenter le système de couleur (ColorDisplay widget, ColorPickerDialog, property)
- [ ] Créer Tool interface, PaintTool, PickerTool et ToolSelector widget
- [X] Ajouter menu File avec Open et Save image
- [ ] Implémenter KdTree avec Point2D, insertion et recherche plus proche voisin
- [ ] Créer MosaicFilter utilisant KdTree pour effet mosaïque
- [ ] Ajouter MosaicDialog et entrée menu Filter > Mosaic Effect
- [ ] Implémenter EnergyCalculator pour calcul gradient des pixels
- [ ] Créer SeamCarver avec programmation dynamique et backtracking
- [ ] Ajouter SeamCarvingDialog et entrée menu Edit > Resize with Seam Carving
- [ ] Compléter tests unitaires/intégration et Javadoc pour toutes les features
