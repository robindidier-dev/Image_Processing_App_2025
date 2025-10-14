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

`git fetch` - Télécharger SANS modifier

`git pull` - Télécharger ET appliquer

`git rebase` - Rejouer vos commits sur une nouvelle base

`git merge` - Fusionner deux branches

## Workflow 

### Synchronisation avec l'équipe
```bash
# 1. Récupérer le travail de l'équipe
git checkout main
git pull origin main

# 2. Mettre à jour votre branche feature
git checkout votre-nom/votre-feature
git rebase main

# 3. Résoudre les conflits si nécessaire
# Éditer les fichiers en conflit, puis :
git add [fichiers]
git rebase --continue
```

### Pendant le développement

```bash
# 1. Travailler sur votre branche
git checkout votre-nom/votre-feature

# 2. Faire des commits réguliers
git add .
git commit -m "message"

# 3. Push régulier vers origin (optionnel)
git push origin votre-nom/votre-feature
```

### Avant de créer une Pull Request

```bash
# 1. Sync avec main (au cas où quelqu'un a mergé)
git checkout main
git pull origin main

# 2. Rebase votre feature sur main à jour
git checkout votre-nom/votre-feature
git rebase main

# 3. Push vers origin pour la PR
git push origin votre-nom/votre-feature
```

### Après le merge d'une PR

```bash
# 1. Mettre à jour votre main local
git checkout main
git pull origin main

# 2. Supprimer votre branche feature locale (si terminée)
git branch -d votre-nom/votre-feature

# 3. Créer un nouvelle branche pour une nouvelle feature
git branch -b votre-nom/nouvelle-feature
```

## Gérer les conflits

```bash
git rebase main
# CONFLICT (content): Merge conflict in MainController.java

# 1. Ouvrir les fichiers en conflit
# Chercher les marqueurs : <<<<<<<, =======, >>>>>>>

# 2. Résoudre manuellement

# 3. Ajouter les fichiers résolus
git add MainController.java

# 4. Continuer le rebase
git rebase --continue

# OU annuler si trop compliqué
git rebase --abort
```