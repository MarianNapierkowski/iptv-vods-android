# Stream Viewer Android App

This is a native Android application for the Stream Viewer project, built with **Kotlin** and **Jetpack Compose**.

## Features
-   **Login**: Connect to your Stream Viewer backend.
-   **Browsing**: Toggle between Movies and Series.
-   **TV Support**: D-Pad navigation support for Android TV.
-   **Playback**: Native MKV playback using **Media3 / ExoPlayer**.
-   **Details**: View Plot, Cast, Director, and Episode lists.

## Prerequisites
-   Android Studio Iguana or later (recommended).
-   JDK 17.
-   A running instance of the Stream Viewer Backend (Python).

## Setup
1.  Open the `android_app` folder in Android Studio.
2.  Allow Gradle to sync.
3.  Connect an Android device or start an Emulator (API 26+).
4.  Run the `app` configuration.

## Configuration
On the Login screen, enter the **internal IP address** of your computer running the Python backend (e.g., `http://192.168.1.5:5000`). **Do not use `localhost`** if testing on a real device or standard emulator, as `localhost` refers to the device itself.

## Architecture
-   **UI**: Jetpack Compose (Single Activity).
-   **Navigation**: Navigation Compose.
-   **Networking**: Retrofit + OkHttp + Gson.
-   **Player**: Media3 ExoPlayer.
