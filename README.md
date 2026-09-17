# Pictionis

Application Android multijoueur inspirée de Pictionary : un joueur dessine
un mot secret, les autres tentent de le deviner en temps réel.

![Aperçu de Pictionis](docs/apercu.png)

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

```
app/src/main/java/com/pictionis/ap/
├── auth/          Authentification
├── model/         Game, Stroke, Point, ChatMessage
├── ui/components/ Canvas de dessin Compose
├── ui/navigation/ Navigation authentification et jeu
├── ui/screen/     Écrans de connexion, lobby et partie
├── ui/theme/      Thème Material 3
├── utils/         Bibliothèque de mots
└── viewModel/     État et synchronisation Firebase
```

## Lancer le projet

Prérequis : Android Studio avec le SDK 36, JDK 11, un appareil ou
émulateur sous Android 8.0 minimum, et un projet Firebase configuré pour
le package `com.pictionis.ap` (Authentication e-mail/mot de passe +
Realtime Database activés).

Placez votre `google-services.json` dans `app/`, puis :

```bash
./gradlew assembleDebug    # gradlew.bat sous Windows
./gradlew test
```

L'APK de debug est généré dans `app/build/outputs/apk/debug/`.

## Limites connues

- Pas de système de score ni de chronomètre
- Sélection du mot entièrement automatique
- Tests limités aux tests de démarrage générés par le projet
- Les règles Firebase doivent être durcies avant toute mise en production
