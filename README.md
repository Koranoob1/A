# Pass Black — Proton Pass Pure AMOLED patch

An independent Morphe patch project for the Android Proton Pass app. It changes the dark palette's
main background colors to AMOLED black (`#000000`), input surfaces to `#0A0A0A`, and input outlines
to `#242424`, while leaving the Proton accent palette intact.

## Target

- App: Proton Pass F-Droid
- Package: `proton.android.pass.fdroid`
- Target version: `1.41.5` (pinned; no claim of compatibility with later versions)
- Patch: `Pure AMOLED`

The uploaded APK contains the named DEX class `Lproton/android/pass/commonui/api/PassPalette;` and
its static initializer. The patch hooks that initializer after the app's original palette setup.
It updates these palette values: `EerieBlack`, `DarkGunmetal`, `ValleyNight`, `SmokyBlack`,
`ShadowyCove`, `StormyNight100`, `MysticNightFall`, `NightHorizon`, and `DarkSlateGray`.

## Build

From this directory, with the Morphe patch development prerequisites and repository credentials
configured as described in the official template documentation:

```bash
./gradlew buildAndroid
```

The output should be `patches/build/libs/patches-*.mpp`.

This workspace currently contains the patch **source project**. A compiled `.mpp` has not yet been
produced or tested in Morphe Desktop/Manager in this environment. Do not treat it as a ready-to-install
patch bundle until the build succeeds and it has been applied to the exact target APK.

## Apply

After building the `.mpp`, load it in Morphe Desktop or add the published patch source to Morphe
Manager, select `Pure AMOLED`, and patch the original Proton Pass 1.41.5 APK. The output APK is
re-signed by Morphe; Android may require uninstalling the originally signed original first. Back up
any important data and verify sign-in, sync, autofill, passkeys, and biometric unlock after patching.

## Scope and caveats

- This patch modifies the app's `PassPalette` dark-mode palette only. It does not guarantee that
  every third-party/core UI surface, image, gradient, or system dialog becomes pure black.
- The patch is version-pinned. Do not force it onto another Proton Pass version; fingerprints and
  field layout must be rechecked first.
- Proton Pass is GPL-3.0. This independent patch project follows GPL-3.0; retain upstream notices
  when distributing derivative code. This project is not affiliated with or endorsed by Proton AG
  or Morphe.
