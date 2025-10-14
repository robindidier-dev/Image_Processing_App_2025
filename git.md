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

- **Pendant le dev (petits commits, rebase régulier, pas de merge)**
  ```bash
  # travailler sur votre branche
  git checkout votre-nom/votre-feature
  
  # commit régulier
  git add .
  git commit -m "message clair"

  # se remettre à jour proprement avec les features des autres
  git fetch origin
  git rebase origin/main

  # push sur votre branche distante (optionnel)
  git push origin votre-nom/votre-feature
  ```

- **Avant la PR**
  ```bash
  git fetch origin # télécharger les dernière modif du remote
  git checkout votre-nom/votre-feature
  git rebase origin/main # comparer main avec sa branche 
  # s'il y a des conflits: éditer → git add fichier.edité → git rebase --continue (jusqu'a qu'il n'y ait pu de conflit)

  # après rebase, pousser en protégé (rebase implique une réecriture de l'historique d'où le "force)
  git push --force-with-lease origin votre-nom/votre-feature
  ```
  - Créez la PR vers `main`. La PR ne doit montrer que vos commits.
  - Si c’est la PR provoque peu de conflits: bouton “Resolve conflicts” dans GitHub.


- **Après merge de la PR**
  ```bash
  git checkout main 
  git pull origin main # mettre a jour main en local
  git branch -d votre-nom/votre-feature #(delete la branche local de travail si feature terminé (optionnel))
  # supprimer la branche distante dans l’UI GitHub (optionnel)
  ```

### Vérifs rapides (utile à tous)
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