# Target APK inspection record

Inspected input: `ProtonPass-Android.apk`

Verified from the binary Android manifest:

- Application label: `Proton Pass`
- Package: `proton.android.pass`
- Version name: `1.41.5`

Verified from DEX:

- `Lproton/android/pass/commonui/api/PassPalette;` exists.
- It contains a static `<clinit>()V` initializer.
- The initializer has 14 registers and ends with `return-void`.
- The palette fields patched by the source are present as static `J` fields:
  `EerieBlack`, `DarkGunmetal`, `ValleyNight`, `SmokyBlack`, `ShadowyCove`,
  `StormyNight100`, `MysticNightFall`, `NightHorizon`, and `DarkSlateGray`.
- The field values are assigned by the palette initializer, so the patch inserts its overrides
  immediately before the initializer's final `return-void`.

This is static inspection only. The Morphe build and runtime patch application have not been
executed in this environment.
