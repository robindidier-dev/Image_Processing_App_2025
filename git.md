# Guide Git - Travail Collaboratif

## Architecture des branches

```
LOCAL
├── main                      # Branche stable, suit origin/main
└── nom/feature               # Votre branche de feature pour développer

REMOTE origin 
├── main                      # Branche principale 
├── paulantoine/feature       # Feature de PA
├── robin/feature             # Feature de Robin
├── adrien/feature            # Feature d'Adrien
└── feedback                  
```

## Commandes essentielles

- `git fetch` - Télécharger SANS modifier
- `git pull` - Télécharger ET appliquer  
- `git merge` - Fusionner deux branches
- `git branch` - Lister/créer des branches
- `git checkout` - Changer de branche
- `git status` - Voir l'état du repo

## Workflow 

### Début de session, création d'une branche

```bash
# 1. Récupérer les dernières modifs
git fetch origin
git checkout main
git pull origin main

# 2. Créer une nouvelle branche
git checkout -b votre-nom/nouvelle-feature
```

### Pendant le dev (petits commits, merge régulier pour se mettre à jour)

```bash
# travailler sur votre branche
git checkout votre-nom/votre-feature
  
# commit régulier
git add .
git commit -m "message clair"

# se remettre à jour avec les features des autres (via merge)
git fetch origin
git merge origin/main

# push sur votre branche distante (optionnel)
git push origin votre-nom/votre-feature
```

### Avant la PR

```bash
git fetch origin # télécharger les dernière modif du remote
git checkout votre-nom/votre-feature
git merge origin/main # intégrer main dans sa branche

# s'il y a des conflits: éditer → git add fichier.edité → git commit

# pousser normalement (pas de force nécessaire avec merge)
git push origin votre-nom/votre-feature
```
#### En cas de problème
- **Merge raté** : `git merge --abort`
- **Branche cassée** : `git checkout main && git checkout -b nouvelle-branche`
- **Push refusé** : vérifier qu'on est sur la bonne branche

### Après merge de la PR

  ```bash
  git checkout main 
  git pull origin main # mettre a jour main en local

  git branch -d votre-nom/votre-feature # delete la branche local de travail si feature terminé
  git branch -D nom-de-la-branche
  # pour forcer la suppression (même si pas mergée)

  # supprimer la branche distante
  git push origin --delete votre-nom/votre-feature
  # supprime la branche distante (ou passé par l'UI GitHub)

  git fetch --prune origin # pour mettre a jour les branches distantes existantes
  ```

## Vérifs rapides 

```bash
# voir l'etat de vos branches de travail
git log --oneline --graph

# voir les commits que vous avez fait qui ne sont pas encore dans main
git log --oneline --graph origin/main..HEAD

# voir les commits que vous avez fait qui ne sont pas encore poussés sur votre branche distante
git log --oneline --graph origin/votre-nom/votre-feature..HEAD

# observer l'arbre de travail
git log --oneline --graph --decorate --all  
``