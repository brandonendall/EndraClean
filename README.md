# EndraClean

**[Download EndraClean — Run 39 consolidated (0.39.2-gold)](https://github.com/brandonendall/EndraClean/raw/refs/heads/main/downloads/EndraClean.apk)**

Run 39 is ready for physical-device testing, not final release approval. It adds a saved User Apps scan profile and Manage Apps, hides zero-cache results, and shows the approved Hydra cleaning overlay with real processing progress and Stop. System Apps remain informational and cannot be cleaned. The Run 38 Help dialog, Home artwork, launcher icon, and original signing identity are preserved.

50 current automated checks pass (13 permission/layout checks rerun for this visual revision; 37 unchanged regression checks retained), including a simulated two-app Settings transition with the same overlay window. This consolidated revision centers setup over dimmed Home, implements the locked Hydra Permissions reference with ornate gold panels, native live statuses and functional actions, and removes the ordinary-transition overlay hide. Android versionCode 41 orders this update after the earlier Run 39; it remains the Run 39 testing milestone. Physical-device coverage and data-retention checks remain pending. Android 8.0/API 26 or newer is required. JP6S was announced with Android 8.1, but authorized installation, permissions, and cleaning on correctional tablets remain untested. JP5 compatibility is not established. Android/OEM security controls may suppress overlays on sensitive Settings screens; no protections are bypassed.

Install over the existing app with the matching certificate; update installation and data retention still need physical-device confirmation. Do not uninstall merely to update. The downloadable APK is newer than the historical source currently in this repository.

Download integrity: [SHA-256 checksum](downloads/SHA256SUMS).

## Earlier source documentation

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
