# Guide MVC simplifié

## 1. Trois couches qui collaborent

```
┌─────────┐     ┌────────────┐     ┌─────────┐
│  VIEW   │ ──► │ CONTROLLER │ ──► │  MODEL  │
└─────────┘     └────────────┘     └─────────┘
```

- **VIEW** : l’interface (boutons, menus, canevas).
- **CONTROLLER** : coordonne ce que la vue demande et ce que le modèle fournit.
- **MODEL** : manipule et stocke les données (image, couleurs, outils).

## 2. Organisation des fichiers

```
src/main/java/imageprocessingapp/
├── view/        → composants graphiques réutilisables
├── controller/  → classes qui gèrent les interactions
├── model/       → logique métier, outils, structures
└── service/     → services techniques (ex : Canvas)

src/main/resources/imageprocessingapp/
├── view/        → vues FXML
├── dialogs/     → fenêtres modales FXML
└── style/       → feuilles de style CSS
```

## 3. Scénario example 

1. **Ouverture de fichier**
   - L’utilisateur choisit `File > Open`.
   - `MainController.openImage()` déclenche un `FileChooser`.
   - Si le fichier est validé, `ImageModel.setImage(...)` charge l’image et `DrawingService.resizeCanvasToImage(...)` aligne le canevas.
   - La `ImageView` se met à jour via le binding `currentImageProperty`.

2. **Sélection d’un outil de dessin**
   - L’utilisateur active le pinceau dans la barre d’outils.
   - `ToolSelectorController` instancie `PaintTool` et l’injecte dans `MainController.activeToolProperty`.
   - Les événements souris sur le canevas déclenchent `tool.onMouseDragged(...)` qui appelle `ImageModel.drawCircle(...)`.

3. **Application de l’effet mosaïque**
   - L’utilisateur choisit `Filters > Mosaic Effect`.
   - `MainController.openMosaicDialog()` charge `MosaicDialog.fxml`, configure `MosaicDialogController` avec `ImageModel` et la propriété `currentImage`.
   - Le slider du dialogue modifie `mosaicSlider.valueProperty`; le listener appelle `updatePreview(...)`.
   - `MosaicDialogController` délègue à `MosaicFilterService.applyMosaic(...)`, qui crée la nouvelle image via `MosaicFilter`.
   - Lorsqu’on confirme, `okPressed()` met à jour `ImageModel` et `currentImageProperty`, la vue rafraîchit immédiatement l’affichage.

4. **Sauvegarde**
   - `MainController.saveImage()` agrège l'image originale et le canevas (`DrawingService.createCompositeImage()`), puis écrit le résultat sur le disque.

5. **Redimensionnement avec Seam Carving**
   - L'utilisateur choisit `Edit > Resize with Seam Carving`.
   - `MainController.handleSeamCarving()` appelle `SeamCarvingDialogController.show()`.
   - Le dialogue charge `SeamCarvingDialog.fxml` avec deux sliders (largeur/hauteur).
   - Les sliders sont initialisés avec les dimensions actuelles de l'image.
   - À chaque modification des sliders, `updatePreview()` appelle `SeamCarvingService.resize()`.
   - `SeamCarvingService` orchestre la suppression de seams via `SeamCarver.resizeOptimized()`.
   - La prévisualisation se met à jour en temps réel via `currentImageProperty`.
   - Lors de la confirmation, `okPressed()` met à jour `ImageModel` et ferme le dialogue.

6. **Rotation d'image**
   - L'utilisateur choisit `Edit > Clockwise Rotation` ou `Counterclockwise Rotation`.
   - `MainController.applyRotation()` capture d'abord le canvas de dessin (`DrawingService.snapshotCanvas()`).
   - L'image de fond est tournée via `RotateOperation.apply()` sur `ImageModel`.
   - Le canvas est également tourné et réappliqué sur le nouveau canvas.
   - `DrawingService.resizeCanvasToImage()` ajuste le canvas aux nouvelles dimensions.

7. **Symétrie (miroir)**
   - L'utilisateur choisit `Edit > Horizontal Symmetry` ou `Vertical Symmetry`.
   - `MainController.applySymmetry()` suit le même pattern que la rotation.
   - `SymmetryOperation.apply()` retourne l'image selon l'axe spécifié.
   - Le canvas de dessin est également symétrisé et réappliqué.

8. **Crop (découpe)**
   - L'utilisateur choisit `Edit > Crop`.
   - `MainController.startCropping()` active `CropTool` et configure le `maskCanvas` pour l'overlay.
   - L'utilisateur dessine un rectangle de sélection sur l'image.
   - `CropTool` gère les événements souris et met à jour visuellement la zone de sélection.
   - Lors de la validation, `MainController.applyCropping()` crée une image composite (fond + canvas).
   - `CropOperation.apply()` extrait la zone sélectionnée de l'image composite.
   - L'image et le canvas sont redimensionnés aux nouvelles dimensions.

## 4. Points clés du projet

- `MainView.fxml` : structure principale de l'interface.
- `MainController` : contrôleur central qui coordonne toutes les interactions.
- `ImageModel` : encapsule l'image courante et fournit l'accès aux pixels.
- `DrawingService` : prépare et gère le `Canvas` de dessin.
- `MosaicDialogController` + `MosaicFilterService` : appliquent l'effet mosaïque.
- `SeamCarvingDialogController` + `SeamCarvingService` : redimensionnent l'image via Seam Carving.
- `RotateOperation`, `SymmetryOperation`, `CropOperation` : opérations géométriques sur l'image.
- `SeamCarver` + `EnergyCalculator` : algorithme de Seam Carving (calcul d'énergie, programmation dynamique).

## 5. Raisons de ce découpage

- Rôle distinct pour chaque couche → maintenance plus simple.
- Tests unitaires possibles sur le modèle sans lancer JavaFX.
- Interface modifiable sans impacter la logique métier.


