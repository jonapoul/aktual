package aktual.core.ui

import androidx.compose.ui.text.TextStyle

// Inter's tabular figures and disambiguated glyphs, for code and IDs in place of a monospace font
fun TextStyle.disambiguated(): TextStyle = copy(fontFeatureSettings = DISAMBIGUATED)

fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

private const val DISAMBIGUATED = """"tnum", "ss02""""
