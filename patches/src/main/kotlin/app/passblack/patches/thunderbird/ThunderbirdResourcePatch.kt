package app.passblack.patches.thunderbird

import app.morphe.patcher.patch.resourcePatch

internal val thunderbirdResourcePatch = resourcePatch {
    execute {
        val colors = setOf(
            "darkSurface",
            "darkSurfaceBright",
            "darkSurfaceContainer",
            "darkSurfaceContainerHigh",
            "darkSurfaceContainerHighest",
            "darkSurfaceContainerLow",
            "darkSurfaceContainerLowest",
            "darkSurfaceDim",
            "design_dark_default_color_background",
            "design_dark_default_color_surface"
        )

        document("res/values/colors.xml").use { doc ->
            val nodes = doc.getElementsByTagName("color")
            for (i in 0 until nodes.length) {
                val node = nodes.item(i)
                val name = node.attributes
                    ?.getNamedItem("name")?.nodeValue
                if (name in colors) {
                    node.textContent = "#000000"
                }
            }
        }
    }
}
