# Pictionis

Application Android multijoueur inspirée de Pictionary : un joueur dessine
un mot secret, les autres tentent de le deviner en temps réel.

## Fonctionnalités

- Création de compte et authentification via Firebase (e-mail / pseudo)
- Création d'une partie avec identifiant unique partageable
- Lobby temps réel : liste des joueurs, identification de l'organisateur
- Démarrage de la partie par l'organisateur dès deux joueurs présents
- Tirage automatique d'un mot depuis la bibliothèque intégrée
- Canvas tactile avec synchronisation des traits entre joueurs
- Dessin réservé au joueur désigné, effacement du canvas partagé
- Chat intégré pour proposer les réponses
- Reprise d'une partie en cours depuis l'accueil

## Stack

Kotlin 2.0.21 · Jetpack Compose · Material 3 · Firebase Authentication ·
Firebase Realtime Database · Android SDK 36 (minimum API 26) ·
Android Gradle Plugin 8.13.0

Les parties, les traits et le chat sont stockés dans la Realtime Database.
Les ViewModels observent les changements et mettent à jour l'interface
sans rechargement.

## Architecture
