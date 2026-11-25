package app.passblack.patches.thunderbird

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.ApkFileType

private object ThunderbirdThemeFingerprint : Fingerprint(
    definingClass = "Lnet/thunderbird/components/ui/bolt/theme/thunderbird/ThemeColorsKt;",
    name = "<clinit>",
    returnType = "V",
    parameters = emptyList()
)

@Suppress("unused")
val thunderbirdAmoledPatch = patch(
    name = "Pure AMOLED",
    description = "Sets Thunderbird dark theme surfaces to true black.",
    default = true
) {
    compatibleWith(
        Compatibility(
            name = "Thunderbird",
            packageName = "net.thunderbird.android",
            apkFileType = ApkFileType.APK,
            appIconColor = 0x000000,
            targets = listOf(
                AppTarget(version = "25.0")
            )
        )
    )

    execute {
        val method = ThunderbirdThemeFingerprint.method
        val instructions = method.implementation!!.instructions

        val constructors = instructions.withIndex().filter {
            it.value.opcode.toString()
                .replace("-", "_")
                .equals("INVOKE_DIRECT_RANGE", true) &&
            (it.value as? ReferenceInstruction)?.reference.toString().contains("ThemeColorScheme;-><init>")
        }

        require(constructors.size == 2) {
            "Unexpected Thunderbird theme initializer"
        }

        method.addInstructions(
            constructors.last().index,
            """
            const-wide v128, 0xff00000000000000L
            const-wide v130, 0xff00000000000000L
            const-wide v132, 0xff00000000000000L
            const-wide v138, 0xff00000000000000L
            const-wide v140, 0xff00000000000000L
            const-wide v142, 0xff00000000000000L
            const-wide v144, 0xff00000000000000L
            const-wide v146, 0xff00000000000000L
            """.trimIndent()
        )
    }
}
