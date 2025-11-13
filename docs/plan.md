# Plan de développement 

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

- Créer `MosaicDialog.fxml` avec Slider (nombre seeds), ChoiceBox (distribution)
- Controller avec application du filtre et preview
- Ajouter Menu > Filters > Mosaic Effect
- Intégration avec MainController

**Merge Etape 2:** kdtree-model → mosaic-filter → mosaic-ui

---

### **Etape 2.5 : Opérations géométriques**

#### Paul-Antoine - Symétrie & infrastructure opérations

**Branch:** `paulantoine/symmetry`

- Créer le sous-package `model/operations/`
- Introduire l'interface `Operation` et `SymmetryOperation`
- Intégrer les actions de symétrie au `MainController` et au menu Edit

#### Robin - Rotation d’image

**Branch:** `robin/rotate`

- Implémenter `RotateOperation` (90/180/270°) dans `model/operations/`
- Ajouter les entrées de menu Edit correspondantes dans l’UI
- Couvrir par des tests unitaires sur petites images

#### Adrien - Outils de crop et préparation compression

**Branch:** `adrien/cropping`

- Implémenter `CropOperation` et l’outil de sélection associé
- Gérer l’overlay de sélection et son intégration dans la barre d’outils
- Exposer les actions de crop/compression via le menu Edit (sans dialogues)

**Merge Etape 2.5:** symmetry → rotate → cropping

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

**Branch:** `paulantoine/seam-ui`

- Créer `SeamCarvingDialog.fxml` avec TextField (largeur/hauteur)
- Controller avec boucle de suppression de seams
- Ajouter Menu > Edit > Resize with Seam Carving
- Option: bouton preview pour visualiser prochaine couture

#### Adrien - UI Seam Carving

**Branch:** `adrien/seam-algorithm`

- Créer `SeamCarver` dans `model/filters/`
- Méthode `computeCumulativeEnergy` (programmation dynamique)
- Méthode `findSeam` (backtracking depuis min dernière ligne)
- Méthode `removeSeam` (décalage pixels)
- Tests pour vertical ET horizontal

**Merge Etape 3:** energy-calculator → seam-algorithm → seam-ui

---

### **Etape 4 : Finitions & Tests**

#### Paul-Antoine

#### Robin

#### Adrien

---

### To-dos

- [x] Supprimer le package teamteacher et mettre à jour module-info.java
- [x] Créer MainApp.java, MainController.java et MainView.fxml avec menu/toolbar/imageview
- [x] Implémenter le système de couleur (ColorDisplay widget, ColorPickerDialog, property)
- [x] Créer Tool interface, PaintTool, PickerTool et ToolSelector widget
- [x] Ajouter menu File avec Open et Save image
- [x] Implémenter KdTree avec Point2D, insertion et recherche plus proche voisin
- [x] Créer MosaicFilter utilisant KdTree pour effet mosaïque
- [x] Ajouter MosaicDialog et entrée menu Filter > Mosaic Effect
- [x] Introduire `model/operations` avec une interface Operation 
- [ ] Implémenter EnergyCalculator pour calcul gradient des pixels
- [ ] Créer SeamCarver avec programmation dynamique et backtracking
- [ ] Ajouter SeamCarvingDialog et entrée menu Edit > Resize with Seam Carving
- [ ] Compléter tests unitaires/intégration et Javadoc pour toutes les features
