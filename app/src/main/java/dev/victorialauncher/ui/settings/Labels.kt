// SPDX-License-Identifier: GPL-3.0-or-later
package dev.victorialauncher.ui.settings

import androidx.annotation.StringRes
import dev.victorialauncher.R
import dev.victorialauncher.data.AppFont
import dev.victorialauncher.data.ClockStyle
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
fun ClockStyle.labelRes(): Int = when (this) {
    ClockStyle.CLASSIC -> R.string.clock_style_classic
    ClockStyle.STACKED -> R.string.clock_style_stacked
    ClockStyle.MINIMAL -> R.string.clock_style_minimal
    ClockStyle.ANALOG -> R.string.clock_style_analog
    ClockStyle.DIGITAL_CARD -> R.string.clock_style_digital_card
    ClockStyle.DAY_FOCUS -> R.string.clock_style_day_focus
    ClockStyle.TECH_HUD -> R.string.clock_style_tech_hud
    ClockStyle.SYSTEM_MONITOR -> R.string.clock_style_system_monitor
    ClockStyle.MINIMAL_SPECS -> R.string.clock_style_minimal_specs
    ClockStyle.RETRO_TERMINAL -> R.string.clock_style_retro_terminal
}

@StringRes
fun dev.victorialauncher.data.ThemedIconStyle.labelRes(): Int = when (this) {
    dev.victorialauncher.data.ThemedIconStyle.MATERIAL_YOU -> R.string.settings_themed_icons_style_material_you
    dev.victorialauncher.data.ThemedIconStyle.MINIMALIST -> R.string.settings_themed_icons_style_minimalist
}