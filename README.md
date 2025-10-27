# ImageProcessingApp

Une application JavaFX de traitement d'image développée en équipe avec les fonctionnalités suivantes :
- **Outils de dessin** (pinceau, pipette)
- **Effet mosaïque** utilisant un KdTree
- **Seam Carving** pour redimensionner intelligemment les images
- **Système de couleurs** avec sélecteur RGB

## Architecture du projet

### Structure du projet

```
src/main/java/imageprocessingapp/
├── MainApp.java                  # Point d'entrée de l'application
├── model/                        # Logique métier et données
│  ├── ImageModel.java            # Modèle principal de l'image
│  ├── ColorUtils.java            # Utilitaires pour les couleurs
│  ├── tools/                     # Outils de dessin
│  │  ├── Tool.java               # Interface des outils
│  │  ├── PaintTool.java
│  │  └── PickerTool.java     
│  ├── ColorUtils.java 
│  ├── filters/                   # Filtres/effets (mosaïque, seam carving)
│  └── structures/                # Structures de données (KdTree)
├── view/                         # Composants d'interface utilisateur
│  └── components/                # Widgets réutilisables
│     └── ColorDisplay.java
├── controller/                      # Logique de contrôle
│  ├── MainController.java           # Contrôleur principal
│  ├── ToolSelectorController.java        # Contient la logique ToggleGroup
│  └── ColorPickerDialogController.java   # Contient la logique d'affichage
└── service/
   └── DrawingService.java

src/main/resources/imageprocessingapp/
├── view/                        
│  └── MainView.fxml              # Interface principale (FXML)
└── dialogs/                      # Fenêtres de dialogue

src/test/java/imageprocessingapp/
├── model/                        # Tests unitaires du model
└── integration/                  # Tests d'intégration
```


Cette structure suit les **bonnes pratiques JavaFX** et les principes de développement logiciel :

#### **Séparation des responsabilités (MVC)**

Le pattern **Model-View-Controller** organise le code selon trois responsabilités distinctes :

##### **`model/` - Logique métier**
- **Données persistantes** : Images, couleurs, états d'application
- **Règles métier** : Calculs, transformations, validations
- **Logique pure** : Indépendante de l'interface utilisateur
- **Réutilisabilité** : Peut être testé et utilisé sans UI

##### **`view/` + FXML - Interface utilisateur**
- **Composants visuels** : Boutons, labels, canvas
- **Layout et style** : Positionnement, CSS, FXML
- **Widgets réutilisables** : Composants personnalisés
- **Présentation** : Comment les données sont affichées

##### **`controller/` - Coordination**
- **Interactions utilisateur** : Clics, saisies, événements
- **Coordination** : Fait le lien entre Model et View
- **État de l'interface** : Gestion des boutons, sélections
- **Logique de contrôle** : Quand et comment réagir

##### **`service/` - Logique technique**
- **Opérations complexes** : Manipulation de canvas, composition d'images
- **Logique technique** : Pas métier, pas UI, mais nécessaire
- **Réutilisabilité** : Peut être utilisé par plusieurs contrôleurs


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

### Composants principaux

- **MainApp** : Lance l'application JavaFX
- **MainController** : Gère les interactions utilisateur et coordonne les composants
- **ImageModel** : Représente une image modifiable avec accès aux pixels
- **MainView.fxml** : Interface utilisateur avec menu, toolbar et zone d'image
- **DrawingService** : Gère les opérations sur le canvas de dessin
- **ToolSelectorController** : Gère la sélection des outils (ToggleGroup)

##  Comment lancer

### Via Maven 
```bash
# Compiler le projet
mvn compile

# Lancer l'application
mvn javafx:run
```

### Via IntelliJ IDEA
   - Clic droit sur `MainApp.java` → `Run 'MainApp.main()'`
   - Ou utiliser le bouton ▶️ vert à côté de la classe `MainApp`

## Développement en équipe

Le projet est développé par **Robin**, **Adrien** et **Paul-Antoine** 

Voir `plan.md` pour le détail des tâches et la roadmap de développement.
