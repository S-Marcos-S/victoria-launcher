// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.settings

import androidx.annotation.StringRes
import dev.victorialauncher.R
import dev.victorialauncher.data.AppFont
import dev.victorialauncher.data.EdgeSide
import dev.victorialauncher.data.TextColorMode

/**
 * Display labels for the settings chips.
 *
 * These used to be rendered straight off the enum constant names, which made the whole
 * settings screen partly untranslatable — and tied user-visible text to identifiers that
 * exist for the code's benefit.
 */
@StringRes
fun AppFont.labelRes(): Int = when (this) {
    AppFont.SYSTEM -> R.string.font_system
    AppFont.SANS_SERIF -> R.string.font_sans_serif
    AppFont.SERIF -> R.string.font_serif
    AppFont.MONOSPACE -> R.string.font_monospace
}

@StringRes
fun TextColorMode.labelRes(): Int = when (this) {
    TextColorMode.AUTO -> R.string.text_colour_auto
    TextColorMode.LIGHT -> R.string.text_colour_light
    TextColorMode.DARK -> R.string.text_colour_dark
}

@StringRes
fun EdgeSide.labelRes(): Int = when (this) {
    EdgeSide.LEFT -> R.string.edge_left
    EdgeSide.RIGHT -> R.string.edge_right
    EdgeSide.BOTH -> R.string.edge_both
}

@StringRes
fun dev.victorialauncher.data.HomeAlignment.labelRes(): Int = when (this) {
    dev.victorialauncher.data.HomeAlignment.LEFT -> R.string.alignment_left
    dev.victorialauncher.data.HomeAlignment.CENTER -> R.string.alignment_center
    dev.victorialauncher.data.HomeAlignment.RIGHT -> R.string.alignment_right
}

@StringRes
fun dev.victorialauncher.data.IconSide.labelRes(): Int = when (this) {
    dev.victorialauncher.data.IconSide.LEFT -> R.string.alignment_left
    dev.victorialauncher.data.IconSide.RIGHT -> R.string.alignment_right
}