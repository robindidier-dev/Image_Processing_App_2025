# ImageProcessingApp

Une application JavaFX de traitement d'image développée en équipe avec les fonctionnalités suivantes :
- **Outils de dessin** (pinceau, pipette, gomme)
- **Effet mosaïque** utilisant un **KdTree**
- **Seam Carving** pour redimensionner intelligemment les images
- **Système de couleurs** avec sélecteur RGB

## Architecture du projet

### Structure du projet

```
src/main/java/
├── module-info.java              # Définition du module Java
└── imageprocessingapp/
   ├── MainApp.java               # Point d'entrée de l'application
   ├── model/                     # Logique métier et données
   │  ├── ImageModel.java         # Modèle principal de l'image
   │  ├── ColorUtils.java         # Utilitaires pour les couleurs
   │  ├── tools/                  # Outils de dessin
   │  │  ├── Tool.java            # Interface des outils
   │  │  ├── PaintTool.java
   │  │  ├── PickerTool.java
   │  │  └── EraseTool.java
   │  ├── filters/                # Filtres/effets (mosaïque, seam carving)
   │  │  ├── MosaicFilter.java
   │  │  ├── EnergyCalculator.java
   │  │  └── SeamCarver.java
   │  └── structures/             # Structures de données (KdTree)
   │     ├── Point2D.java
   │     └── KdTree.java
   ├── view/                      # Composants d'interface utilisateur
   │  └── components/             # Widgets réutilisables
   │     └── ColorDisplay.java
   ├── controller/                # Logique de contrôle
   │  ├── MainController.java     
   │  ├── ToolSelectorController.java# Logique ToggleGroup 
   │  ├── ColorPickerDialogController.java
   │  ├── MosaicDialogController.java
   │  └── SeamCarvingDialogController.java
   └── service/
      ├── DrawingService.java
      └── filters/
         └── MosaicFilterService.java

src/main/resources/imageprocessingapp/
├── view/
│  ├── MainView.fxml              # Interface principale (FXML)
│  └── ToolSelectorView.fxml
├── dialogs/                      # Fenêtres de dialogue
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
   └── MosaicDialog.css

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
- **Service Layer** : Logique technique réutilisable (`DrawingService`)
- **Interface Tool** : Polymorphisme pour les outils de dessin
- **KdTree** : Accélère la recherche du plus proche voisin (mosaïque)
- **Seam Carving** : Redimensionnement via énergie cumulative (programmation dynamique)

### Composants principaux

- **MainApp** : Lance l'application JavaFX
- **MainController** : Interactions utilisateur et coordination
- **ImageModel** : Représente une image modifiable avec accès aux pixels
- **MainView.fxml** : Interface utilisateur avec menu, toolbar et zone d'image
- **DrawingService** : Opérations sur le canvas de dessin
- **MosaicFilterService** : Service applicatif pour l’effet mosaïque
- **ToolSelectorController** : Sélection des outils (ToggleGroup)
- **MosaicFilter** : Effet mosaïque à partir d’un KdTree de seeds
- **EnergyCalculator / SeamCarver** : Calcul d’énergie et suppression de seams
- **MosaicDialog / SeamCarvingDialog** : Paramétrage des filtres (UI)

##  Comment lancer

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

## Développement en équipe

Le projet est développé par **Robin**, **Adrien** et **Paul-Antoine** 

Voir `plan.md` pour le détail des tâches et la roadmap de développement.
