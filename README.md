# EndraClean

EndraClean is a user-app cache cleaner for Android. This is a **source reconstruction** guided by the EndraClean v0.3 handoff and APK, with visual treatment based on the EndraLink source. It is not the original v0.3 source or a claim of binary equivalence.

The same deep navy, raised blue panels, rounded gradient controls and Hydra motif as EndraLink use **gold/yellow** accents here. The launcher emblem and in-app three-headed Hydra are drawn from project-owned vector/canvas shapes.

## Behavior

- Lists **user-installed apps only**. System and updated-system apps cannot be listed, selected or queued.
- Displays cache sizes where Android Usage Access provides storage statistics.
- A user explicitly selects apps and starts a foreground cleaning session with a persistent notification and STOP action.
- A user-enabled accessibility service opens each selected app's Android Settings page, enters Storage, and clicks the control whose complete label is **Clear cache**. It never searches for Clear data / Clear storage and never runs `pm clear`.
- Android Settings remains visible during automated cleaning. English Settings labels are supported; unmatched pages time out and are skipped.

## Build

Open this folder in Android Studio with JDK 17 and Android SDK 35. The project uses Android Gradle Plugin 8.7.3. Build a debug APK using `gradle assembleDebug` (or Android Studio's Build menu). A Gradle wrapper and SDK are not bundled in this reconstruction.

Package ID: `com.endra.clean`. Installing alongside the earlier v0.3 APK may require uninstalling that APK because the original signing key is unavailable; uninstalling can reset this app's permissions.

## Validation still needed

This source has not yet been compiled or tested on the target Samsung phone. Validate permissions, app enumeration, exact Storage and Clear cache labels, STOP, and background notification behavior before treating it as equivalent to the supplied v0.3 APK. Samsung One UI may require narrow matching changes for a specific version.

Reference handoff: `EndraClean-HANDOFF-v0.3.md` supplied separately by the project owner.
