# Kosher-Liquor

Android app for browsing a kosher liquor catalog (categories, products, barcode
lookup, and search with English/Hebrew support).

## Building

This project was modernized from its original 2015 Eclipse/Gradle setup to a
current Android toolchain.

Requirements:

- JDK 17 or newer
- Android SDK with API level 34 (`compileSdk`/`targetSdk = 34`, `minSdk = 21`)
- Android Studio (Giraffe or newer) **or** the bundled Gradle wrapper

Build from the command line:

```bash
./gradlew assembleDebug
```

The APK is produced under `app/build/outputs/apk/`.

Point Gradle at your SDK either by setting the `ANDROID_HOME` environment
variable or by creating a `local.properties` file with:

```
sdk.dir=/path/to/Android/Sdk
```

## Toolchain

- Gradle 8.14.3 (wrapper)
- Android Gradle Plugin 8.5.2
