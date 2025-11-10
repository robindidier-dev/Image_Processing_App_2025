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
   - `MainController.saveImage()` agrège l’image originale et le canevas (`DrawingService.createCompositeImage()`), puis écrit le résultat sur le disque.

## 4. Points clés du projet

- `MainView.fxml` : structure principale de l’interface.
- `MainController` : contrôleur central.
- `ImageModel` : encapsule l’image courante.
- `DrawingService` : prépare et gère le `Canvas`.
- `MosaicDialogController` + `MosaicFilterService` : appliquent l’effet mosaïque.

## 5. Raisons de ce découpage

- Rôle distinct pour chaque couche → maintenance plus simple.
- Tests unitaires possibles sur le modèle sans lancer JavaFX.
- Interface modifiable sans impacter la logique métier.


