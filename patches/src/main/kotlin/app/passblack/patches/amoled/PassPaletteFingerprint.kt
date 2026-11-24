package app.passblack.patches.amoled

import app.morphe.patcher.Fingerprint

/**
 * Proton Pass 1.41.5 keeps its Compose palette in this named class.
 * The initializer is used as a stable hook for the exact APK inspected during development.
 */
object PassPaletteInitializerFingerprint : Fingerprint(
    definingClass = "Lproton/android/pass/commonui/api/PassPalette;",
    name = "<clinit>",
    returnType = "V",
    parameters = emptyList()
)
