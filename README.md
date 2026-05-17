# Chess Master Offline

Offline Android chess game built with Kotlin, Jetpack Compose, MVVM, DataStore, and a local chess rules engine.

## Project Summary

- App name: Chess Master Offline
- Package: `com.pramod.chessmasteroffline`
- Min SDK: 23
- Compile/target SDK: 36
- Build system: Gradle Kotlin DSL with Android Gradle Plugin 9.2.1
- UI: Jetpack Compose Material 3
- Storage: DataStore Preferences
- Permissions: none
- Network: not required

## Folder Structure

```text
app/src/main/java/com/pramod/chessmasteroffline/
  MainActivity.kt
  ads/AdManagerPlaceholder.kt
  ai/ChessAi.kt
  data/AppSettings.kt
  data/SavedGameRepository.kt
  data/SettingsRepository.kt
  engine/ChessEngine.kt
  engine/ChessModels.kt
  engine/ChessNotation.kt
  engine/Fen.kt
  ui/AppScreen.kt
  ui/ChessMasterApp.kt
  ui/ChessUiState.kt
  ui/ChessViewModel.kt
  ui/components/ChessBoard.kt
  ui/screens/Screens.kt
  ui/theme/Theme.kt
app/src/test/java/com/pramod/chessmasteroffline/engine/ChessEngineTest.kt
play-store/listing.md
```

## Features

- Legal chess move validation for all pieces
- Check, checkmate, stalemate, castling, en passant, pawn promotion, and insufficient-material draw
- Player vs Player on one device
- Player vs AI with Easy, Medium, and Hard difficulty
- Easy AI uses random legal moves
- Medium AI uses material evaluation
- Hard AI uses minimax with alpha-beta pruning
- Move highlights, last-move highlights, check state, move history, undo, restart, save, and resume
- Board themes, piece styles, sound toggle, and AI difficulty setting
- Premium placeholder screen and ad integration placeholder without shipping ad SDKs

## Build Debug APK

Android 17/API 37 is visible to lint as a newer SDK, but Google's Android 17 setup page currently labels it as the Cinnamon Bun Preview SDK. This project targets SDK 36 for the stable Play release path. Move to SDK 37 after you intentionally test Android 17 behavior changes and decide to opt in.

From the project root:

```powershell
.\gradlew.bat assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Run Unit Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

The chess engine tests cover initial legal moves, pinned-piece validation, castling, en passant, promotion, checkmate, stalemate, and insufficient material.

## Create A Keystore Safely

Create the upload keystore outside the project folder and do not commit it.

```powershell
New-Item -ItemType Directory -Force C:\secure
keytool -genkeypair `
  -v `
  -keystore C:\secure\chess-master-offline-upload.jks `
  -storetype PKCS12 `
  -alias chess-master-offline `
  -keyalg RSA `
  -keysize 4096 `
  -validity 10000 `
  -dname "CN=Chess Master Offline, OU=Mobile, O=Pramod, L=City, S=State, C=IN"
```

Store the passwords in a password manager. Losing the upload key or passwords can block future updates unless Google Play support resets the upload key.

## Configure Signing

Add these values to your user-level Gradle properties file, not this repository:

```text
C:\Users\<you>\.gradle\gradle.properties
```

```properties
CHESS_RELEASE_STORE_FILE=C:/secure/chess-master-offline-upload.jks
CHESS_RELEASE_STORE_PASSWORD=your_store_password
CHESS_RELEASE_KEY_ALIAS=chess-master-offline
CHESS_RELEASE_KEY_PASSWORD=your_key_password
```

## Build Signed Release AAB

```powershell
.\gradlew.bat clean bundleRelease
```

Output:

```text
app/build/outputs/bundle/release/app-release.aab
```

New Google Play apps should be uploaded as Android App Bundles. The Play Console upload still requires your Google Play Developer account. This project does not upload automatically.

## Upload To Google Play Console

1. Create the app in Google Play Console.
2. Choose category: Games > Board.
3. Complete store listing using `play-store/listing.md`.
4. Complete App content sections, including Data safety and target audience.
5. Enroll in Play App Signing when prompted. New apps are required to use Play App Signing.
6. Create an internal testing release first.
7. Upload `app/build/outputs/bundle/release/app-release.aab`.
8. Review warnings, run pre-launch report, then promote through closed/open testing as needed.

## Privacy Notes

The app source manifest requests no internet, location, media, contacts, camera, microphone, or storage permissions. AndroidX adds an app-private signature permission named `com.pramod.chessmasteroffline.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` in the merged manifest for dynamic receiver safety; it is not a dangerous user permission and is not a data access permission.

The app stores only settings plus saved game state locally through DataStore. There is no login, analytics SDK, ad SDK, or internet requirement in this release.

## Pre-Publish Testing Checklist

- Start a Player vs Player game and make legal/illegal moves.
- Verify check, checkmate, stalemate, castling, en passant, and promotion.
- Play at least one game against each AI difficulty.
- Toggle every setting and restart the app.
- Save and resume a game.
- Use undo after a local move and after an AI reply.
- Rotate device or verify portrait lock behavior on phones.
- Test on API 23, API 29, and latest available API emulator/device.
- Build `assembleDebug`, `testDebugUnitTest`, and signed `bundleRelease`.
- Upload to internal testing and review the Play pre-launch report.

## Local Build Verification

Verified in this workspace:

```text
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat bundleRelease
```

If Windows or OneDrive locks generated build folders, stop Gradle and retry:

```powershell
.\gradlew.bat --stop
.\gradlew.bat clean
```
