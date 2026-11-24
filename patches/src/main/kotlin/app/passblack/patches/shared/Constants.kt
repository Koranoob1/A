package app.passblack.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/** Compatibility is intentionally pinned to the source version used for the uploaded APK. */
val PROTON_PASS_COMPATIBILITY = Compatibility(
    name = "Proton Pass F-Droid",
    packageName = "proton.android.pass.fdroid",
    apkFileType = ApkFileType.APK,
    appIconColor = 0x000000,
    targets = listOf(
        AppTarget(version = "1.41.5")
    )
)
