# ImageProcessingApp

Crédits : 
  Robin DIDIER       (https://github.com/Robin1415)
  Paul-Antoine PARIS (https://github.com/Hexa-Da)
  Adrien LEFEBVRE    (https://github.com/adrienlefebvre6)

Application JavaFX de traitement d'image & dessin développée en équipe avec les fonctionnalités suivantes :

**Fonctionnalités basiques**
- **Import d'images** au format jpg ou png ;
- **Export d'images retouchées** au format jpg ou png ;
- **Outils de dessin** (pinceau, pipette, gomme) sur canvas vide ou image accompagné :
  - d'un **système de couleurs** avec sélecteur RGB ;
  - d'un **slider de choix de l'épaisseur** du pinceau et de la gomme.

**Opérations sur les images**
- **Opérations géométriques** (symétries, rotations, crop) ;
- **Effet mosaïque** utilisant un **KdTree** ;
- **Seam Carving** pour redimensionner intelligemment les images.

**Ergonomie**
- **Undo/Redo** pour annuler et refaire les opérations ;
- **Zoom et navigation** (molette de souris, glisser-déposer, reset) ;
- **Raccourcis clavier** pour bon nombre d'opérations.


## Instructions d'utilisation

**Pour dessiner immédiatement** : sélectionner le pinceau, une couleur (via le rectangle de gauche) et le taille avec le slider pour dessiner sur le canvas.

**Pour importer une image** : File > Open. 

**Pour exporter un travail** :  File > Save.

**Appliquer une opération géométrique à une image** : menu Edit.

**Zoom et déplacement** : molette de la souris pour zoomer, clic gauche continu (sans outil sélectionné) pour se déplacer dans le canvas. 

**Effet mosaïque** : Edit > Mosaic effect. Une fenêtre avec prévisualisation s'ouvre. Choisir le nombre de cellules de l'image et distribution aléatoire ou régulière (cellules carré). Appuyer sur Ok pour valider.

**Redimensionnement par seam carving** : Edit > Resize with seam carving. choisir largeur & hauteur désirée et valider avec Ok. Pas de prévisualisation pour des raisons de performances.

**Raccourcis clavier utiles** :
- Ctrl+S : enregistrer l’image ;
- Ctrl+O : ouvrir une image ;
- Ctrl+N : nouveau canevas ;
- Ctrl+W : fermer l’application ;
- Ctrl+R : réinitialiser la vue ;
- Ctrl+Z : annuler ;
- Ctrl+W : rétablir.

##  Comment lancer le projet

### Via Maven 
```bash
# Compiler le projet
mvn compile

# Lancer l'application
mvn javafx:run
```

### Intégration continue locale
```bash
# Compiler et exécuter tous les tests avec le profil CI
mvn -Pci verify
```

### Via IntelliJ IDEA
   - Clic droit sur `MainApp.java` → `Run 'MainApp.main()'`
   - Ou utiliser le bouton ▶️ vert à côté de la classe `MainApp`

## Architecture du projet

### Structure du projet

```
src/main/java/
├── module-info.java                        # Définition du module Java
└── imageprocessingapp/
   ├── MainApp.java                         # Point d'entrée de l'application
   ├── model/                               # Logique métier et données (couche M)
   │  ├── ImageModel.java                   # État courant de l'image + accès pixels
   │  ├── ColorUtils.java                   # Utilitaires purement métiers
   │  ├── tools/                            # Outils de dessin
   │  │  ├── Tool.java
   │  │  ├── PaintTool.java
   │  │  ├── PickerTool.java
   │  │  ├── EraseTool.java
   │  │  └── edit/
   │  │     └── CropTool.java               # Outil de sélection pour le crop
   │  ├── operations/                       # Transformations géométriques
   │  │  ├── Operation.java
   │  │  ├── SymmetryOperation.java
   │  │  ├── RotateOperation.java
   │  │  └── CropOperation.java
   │  ├── filters/                          # Effets métiers réutilisables
   │  │  └── MosaicFilter.java
   │  ├── structures/                       # Structures de données partagées
   │  │  ├── Point2D.java
   │  │  ├── KdTree.java
   │  │  └── EnergyCalculator.java          # Calcul d'énergie (Seam Carving)
   │  └── edit/                             # Algorithmes de traitement
   │     └── SeamCarver.java                # Algorithmes seam carving (DP + remove)
   ├── service/                             # Couche service (couche S)
   │  ├── DrawingService.java               # Logique technique canvas
   │  ├── FileManagementService.java        # Gestion des fichiers (ouvrir, sauvegarder)
   │  ├── ImageOperationService.java        # Opérations sur l'image (rotation, symétrie, crop)
   │  ├── CanvasStateManager.java           # Gestion de l'état du canvas (modifications)
   │  ├── UnsavedChangesHandler.java        # Gestion des modifications non sauvegardées
   │  ├── UndoRedoService.java              # Gestion de l'historique undo/redo
   │  ├── filters/
   │  │  └── MosaicFilterService.java       # Orchestration mosaïque
   │  └── edit/
   │     └── SeamCarvingService.java        # Orchestration seam carving (boucles, tâches)
   ├── controller/                          # Logique de contrôle (couche C)
   │  ├── MainController.java               # Coordination globale
   │  ├── ToolSelectorController.java       # Gestion ToggleGroup outils
   │  ├── DialogCoordinator.java            # Coordination des dialogues modaux
   │  ├── EventHandlerManager.java          # Gestion des événements souris/clavier
   │  ├── ZoomController.java               # Gestion du zoom et de la navigation
   │  ├── ColorPickerDialogController.java
   │  ├── MosaicDialogController.java
   │  └── SeamCarvingDialogController.java  # Dialogue seam carving (UI -> Service)
   └── view/                                # Composants d'interface (couche V)
      └── components/
         └── ColorDisplay.java

src/main/resources/imageprocessingapp/
├── view/
│  ├── MainView.fxml                        # Interface principale (FXML)
│  └── ToolSelectorView.fxml
├── dialogs/
│  ├── ColorPickerDialog.fxml
│  ├── MosaicDialog.fxml
│  └── SeamCarvingDialog.fxml
├── image/
│  ├── pinceau.png
│  ├── pipette.png
│  └── gomme.png
└── style/
   ├── ToolBar.css
   ├── ColorPickerDialog.css
   ├── MosaicDialog.css
   └── SeamCarvingDialog.css

src/test/java/imageprocessingapp/
├── model/                        # Tests unitaires du model (ColorUtils, KdTree, filters)
└── integration/                  # Tests d'intégration (flux MVC, filtres end-to-end)
```


Cette structure suit les **bonnes pratiques JavaFX** et les principes de développement logiciel :

#### **Séparation des responsabilités (MVC)**

Le pattern **Model-View-Controller** organise le code selon trois responsabilités distinctes :

##### **`model/` - Logique métier**
- **Données persistantes** : Images, couleurs, états d'application
- **Règles métier** : Calculs, transformations, validations
- **Logique pure** : Indépendante de l'interface utilisateur
- **Réutilisabilité** : Testable sans UI

##### **`view/` + FXML - Interface utilisateur**
- **Composants visuels** : Boutons, labels, canvas
- **Layout et style** : Positionnement, CSS, FXML
- **Widgets réutilisables** : Composants personnalisés
- **Présentation** : Affichage des données

##### **`controller/` - Coordination**
- **Interactions utilisateur** : Clics, saisies, événements
- **Coordination** : Lien entre Model et View
- **État UI** : Gestion des boutons, sélections
- **Logique de contrôle** : Orchestration des actions

##### **`service/` - Logique technique**
- **Opérations complexes** : Manipulation de canvas, composition d'images
- **Logique technique** : Non métier, non UI, mais nécessaire
- **Réutilisabilité** : Partageable par plusieurs contrôleurs


#### **Flux de données MVC**

```
┌─────────────┐    Événements     ┌─────────────┐    Appels méthodes    ┌─────────────┐
│    VIEW     │ ────────────────► │ CONTROLLER  │ ────────────────────► │    MODEL    │
│ (FXML/Java) │                   │             │                       │             │
└─────────────┘                   └─────────────┘                       └─────────────┘
       ▲                                 │                                       │
       │         Binding/Updates         │              Données                  │
       └─────────────────────────────────┴───────────────────────────────────────┘
```


#### **Organisation des ressources**
- `src/main/resources/` séparé de `src/main/java/` pour les fichiers non-compilés
- FXML, images, CSS dans les ressources
- Code Java compilé dans `src/main/java/`

#### **Décisions architecturales du projet**

**Pourquoi cette structure ?**

1. **Séparation claire des responsabilités** : Chaque classe a un rôle précis
2. **Réutilisabilité** : Les modèles peuvent être testés indépendamment
3. **Maintenabilité** : Modifications isolées dans chaque couche
4. **Évolutivité** : Facile d'ajouter de nouveaux outils ou filtres

**Choix techniques :**
- **JavaFX Properties** : Binding automatique entre Model et View
- **FXML** : Séparation UI/logique pour faciliter les modifications
- **Service Layer** : Logique technique réutilisable (`DrawingService`, `FileManagementService`, etc.)
- **Séparation des responsabilités** : Refactorisation du `MainController` en services dédiés pour améliorer la testabilité
- **Interface Tool** : Polymorphisme pour les outils de dessin
- **KdTree** : Accélère la recherche du plus proche voisin (mosaïque)
- **Seam Carving** : Redimensionnement via énergie cumulative (programmation dynamique)

### Composants principaux

- **MainApp** : Lance l'application JavaFX
- **MainController** : Interactions utilisateur et coordination (refactorisé pour utiliser des services)
- **ImageModel** : Représente une image modifiable avec accès aux pixels
- **MainView.fxml** : Interface utilisateur avec menu, toolbar et zone d'image
- **DrawingService** : Opérations sur le canvas de dessin
- **FileManagementService** : Gestion des fichiers (ouvrir, sauvegarder, nouveau canvas)
- **ImageOperationService** : Opérations sur l'image (rotation, symétrie, crop)
- **CanvasStateManager** : Gestion de l'état du canvas (modifications, sauvegarde)
- **UnsavedChangesHandler** : Gestion des modifications non sauvegardées
- **UndoRedoService** : Gestion de l'historique des modifications (piles undo/redo, limite de 20 états)
- **DialogCoordinator** : Coordination des dialogues modaux (couleur, mosaïque, seam carving)
- **EventHandlerManager** : Gestion des événements souris et clavier
- **ZoomController** : Gestion du zoom (molette de souris) et de la translation (glisser-déposer)
- **MosaicFilterService** : Service applicatif pour l'effet mosaïque
- **ToolSelectorController** : Sélection des outils (ToggleGroup)
- **MosaicFilter** : Effet mosaïque à partir d'un KdTree de seeds
- **EnergyCalculator / SeamCarver** : Calcul d'énergie et suppression de seams
- **MosaicDialog / SeamCarvingDialog** : Paramétrage des filtres (UI)


## Développement en équipe

Le projet est développé par **Robin**, **Adrien** et **Paul-Antoine**. 

Voir `plan.md` pour le détail des tâches et la roadmap de développement.
