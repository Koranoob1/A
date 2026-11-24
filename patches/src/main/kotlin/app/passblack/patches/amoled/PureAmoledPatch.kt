package app.passblack.patches.amoled

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import app.passblack.patches.shared.PROTON_PASS_COMPATIBILITY

@Suppress("unused")
val pureAmoledPatch = patch(
    name = "Pure AMOLED",
    description = "Sets Proton Pass dark-mode backgrounds to true black while retaining the Proton accent palette.",
    default = true
) {
    compatibleWith(PROTON_PASS_COMPATIBILITY)

    execute {
        val initializer = PassPaletteInitializerFingerprint.method
        val instructions = initializer.implementation!!.instructions
        require(instructions.isNotEmpty()) { "PassPalette initializer has no instructions" }
        require(instructions.last().opcode.toString().replace("-", "_").equals("RETURN_VOID", ignoreCase = true)) {
            "Unexpected PassPalette initializer ending; refusing to patch an unknown build"
        }

        // Insert immediately before RETURN_VOID, after Proton's own palette initialization.
        // Compose's sRGB Color(Long) payload is 0xAARRGGBB00000000.
        initializer.addInstructions(
            instructions.size - 1,
            """
                const-wide v0, 0xff00000000000000L
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->EerieBlack:J
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->DarkGunmetal:J
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->ValleyNight:J
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->SmokyBlack:J
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->ShadowyCove:J

                const-wide v0, 0xff0a0a0a00000000L
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->StormyNight100:J
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->MysticNightFall:J

                const-wide v0, 0xff24242400000000L
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->NightHorizon:J
                sput-wide v0, Lproton/android/pass/commonui/api/PassPalette;->DarkSlateGray:J
            """
        )
    }
}
