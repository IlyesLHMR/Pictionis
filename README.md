# Pictionis
Pictionis est une application Android multijoueur inspiree de **Pictionary**. Un joueur dessine un mot secret et les autres joueurs essaient de le deviner en temps reel.

## Fonctionnalites

- Creation de compte avec adresse e-mail et pseudo.
- Connexion et deconnexion avec Firebase Authentication.
- Creation d'une partie partageable avec un identifiant unique.
- Rejoindre une partie existante avec son identifiant.
- Lobby en temps reel avec la liste des joueurs et l'identification du createur.
- Demarrage de la partie par l'organisateur lorsqu'au moins deux joueurs sont presents.
- Attribution automatique d'un mot aleatoire depuis la bibliotheque integree.
- Dessin sur un canvas tactile avec synchronisation des traits entre les joueurs.
- Activation du dessin uniquement pour le joueur designe comme dessinateur.
- Effacement des traits du canvas partage.
- Chat integre a chaque partie pour proposer des reponses.
- Reprise d'une partie en cours depuis l'accueil.
- Interface construite avec Jetpack Compose et Material 3.

## Technologies

- Kotlin 2.0.21
- Android Gradle Plugin 8.13.0
- Jetpack Compose et Material 3
- Android SDK 36, SDK minimum 26
- Firebase Authentication
- Firebase Realtime Database
- ViewModel et architecture Compose reactive

## Prerequis

- Android Studio recent avec le SDK Android 36.
- JDK 11.
- Un projet Firebase configure pour l'application Android `com.pictionis.ap`.
- Un appareil Android ou un emulateur sous Android 8.0 (API 26) ou superieur.

## Installation

1. Clonez le depot puis ouvrez le dossier dans Android Studio.

	```bash
	git clone <url-du-depot>
	cd <dossier-du-projet>
	```

2. Dans Firebase Console, activez **Authentication** avec le fournisseur e-mail/mot de passe ainsi que **Realtime Database**.

3. Telechargez le fichier `google-services.json` de votre application Firebase et placez-le dans le dossier `app/`.

4. Verifiez que l'URL de la Realtime Database utilisee par les ViewModels correspond a votre projet Firebase.

5. Synchronisez Gradle, puis lancez l'application sur un appareil ou un emulateur.

Le fichier `google-services.json` contient la configuration cliente Firebase, mais les regles de securite de la base doivent etre configurees correctement avant toute mise en production.

## Lancer le projet

Depuis la racine du projet :

```bash
# Windows
gradlew.bat assembleDebug
gradlew.bat test

# macOS / Linux
./gradlew assembleDebug
./gradlew test
```

L'APK de debug est genere dans `app/build/outputs/apk/debug/`.

## Utilisation

1. Creez un compte ou connectez-vous.
2. Depuis l'accueil, creez une partie ou rejoignez une partie avec son identifiant.
3. Attendez l'arrivee d'au moins un autre joueur dans le lobby.
4. L'organisateur demarre la partie.
5. Le mot est choisi automatiquement, puis le dessinateur commence a tracer sur le canvas.
6. Les autres joueurs utilisent le chat pour envoyer leurs propositions.

## Architecture

```text
app/src/main/java/com/pictionis/ap/
├── auth/          Gestion de l'authentification
├── model/         Modeles Game, Stroke, Point et ChatMessage
├── ui/components/ Canvas de dessin Compose
├── ui/navigation/ Navigation des parcours authentifie et jeu
├── ui/screen/     Ecrans de connexion, lobby et partie
├── ui/theme/      Theme Material 3
├── utils/         Bibliotheque de mots
└── viewModel/     Etat et synchronisation Firebase
```

Les parties, les traits et le chat sont stockes dans Firebase Realtime Database. Les changements sont observes par les ViewModels afin de mettre a jour l'interface sans rechargement manuel.

## Limites actuelles

- Le projet ne contient pas encore de systeme de score ou de chronometre.
- La selection du mot est actuellement automatique.
- Les tests presents sont principalement les tests Android et unitaires de demarrage du projet.
- Les regles Firebase doivent etre durcies avant un deploiement public.

## Contribution

1. Creez une branche descriptive depuis `main`.
2. Effectuez une modification ciblee et ajoutez les tests necessaires.
3. Verifiez que `gradlew.bat test` passe.
4. Ouvrez une pull request en decrivant le comportement ajoute ou corrige.

## Licence

Aucune licence open source n'est definie dans le depot pour le moment.
