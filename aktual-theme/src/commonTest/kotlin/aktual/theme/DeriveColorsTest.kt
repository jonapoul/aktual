package aktual.theme

import androidx.compose.ui.graphics.Color
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test

// The built-in themes hold upstream's resolved colours, which upstream derives the same way
class DeriveColorsTest {
  @Test
  fun `Derive the built-in accent colours`() {
    for (theme in BuiltIn) {
      assertThat(deriveAccent(theme.accent, theme.surfaces()), name = theme.id.value)
        .isEqualTo(
          AccentSet(
            accent = theme.accent,
            accentContrast = theme.accentContrast,
            accentHover = theme.accentHover,
            accentText = theme.accentText,
            accentBorder = theme.accentBorder,
            accentSubtle = theme.accentSubtle,
            accentSubtleText = theme.accentSubtleText,
          ),
        )
    }
  }

  @Test
  fun `Derive the built-in status colours`() {
    for (theme in BuiltIn) {
      val surfaces = theme.surfaces()
      assertThat(deriveStatus(theme.success, surfaces), name = "${theme.id.value} success").all {
        prop(StatusSet::fill).isEqualTo(theme.noticeBackground)
        prop(StatusSet::fillSubtle).isEqualTo(theme.noticeBackgroundLight)
        prop(StatusSet::text).isEqualTo(theme.noticeText)
        prop(StatusSet::textOnSubtle).isEqualTo(theme.noticeTextLight)
        prop(StatusSet::textStrong).isEqualTo(theme.noticeTextDark)
      }
      assertThat(deriveStatus(theme.warning, surfaces), name = "${theme.id.value} warning").all {
        prop(StatusSet::fill).isEqualTo(theme.warningBackground)
        prop(StatusSet::text).isEqualTo(theme.warningText)
        prop(StatusSet::textStrong).isEqualTo(theme.warningTextDark)
      }
      assertThat(deriveStatus(theme.error, surfaces), name = "${theme.id.value} error").all {
        prop(StatusSet::fill).isEqualTo(theme.errorBackground)
        prop(StatusSet::text).isEqualTo(theme.errorText)
        prop(StatusSet::textStrong).isEqualTo(theme.errorTextDark)
      }
      assertThat(deriveStatus(theme.info, surfaces), name = "${theme.id.value} info").all {
        prop(StatusSet::fill).isEqualTo(theme.infoBackground)
        prop(StatusSet::textStrong).isEqualTo(theme.infoText)
      }
    }
  }

  @Test
  fun `Derive the built-in accent text on other surfaces`() {
    for (theme in BuiltIn) {
      assertThat(theme, name = theme.id.value).all {
        prop(Colors::sidebarItemAccentSelected)
          .isEqualTo(textOn(theme, theme.sidebarBackground, ENHANCED_TEXT_CONTRAST))
        prop(Colors::menuItemTextHeader)
          .isEqualTo(textOn(theme, theme.menuBackground, TEXT_CONTRAST))
        prop(Colors::menuKeybindingText)
          .isEqualTo(textOn(theme, theme.menuBackground, TEXT_CONTRAST))
        prop(Colors::menuAutoCompleteTextHeader)
          .isEqualTo(textOn(theme, theme.menuAutoCompleteBackground, ENHANCED_TEXT_CONTRAST))
        prop(Colors::pillTextHighlighted)
          .isEqualTo(textOn(theme, theme.pillBackgroundLight, ENHANCED_TEXT_CONTRAST))
        prop(Colors::calendarSelectedText).isEqualTo(pickContrast(theme.calendarSelectedBackground))
      }
    }
  }

  @Test
  fun `Work out the accent and status colours a theme leaves out from its surfaces`() {
    val css =
      """
      :root {
        --color-pageBackground: #1b1c1d;
        --color-tableBackground: #222324;
        --color-mobilePageBackground: #1b1c1d;
        --color-accent: #4466ff;
        --color-success: #11aa11;
        --color-noticeText: #00ff00;
      }
      """
        .trimIndent()

    val colors = parseColors(Summary, css)

    val surfaces = Surfaces(Color(0xFF222324), Color(0xFF1b1c1d), Color(0xFF1b1c1d))
    val accent = deriveAccent(Color(0xFF4466ff), surfaces)
    val success = deriveStatus(Color(0xFF11aa11), surfaces)
    val error = deriveStatus(DarkColors.error, surfaces)
    assertThat(colors).all {
      prop(CustomColors::accentText).isEqualTo(accent.accentText)
      prop(CustomColors::accentHover).isEqualTo(accent.accentHover)
      prop(CustomColors::noticeBackground).isEqualTo(success.fill)
      prop(CustomColors::noticeTextDark).isEqualTo(success.textStrong)
      prop(CustomColors::noticeText).isEqualTo(Color(0xFF00ff00))
      prop(CustomColors::errorText).isEqualTo(error.text)
    }
  }

  private fun textOn(theme: Colors, surface: Color, contrast: Double) =
    nearestWithContrast(theme.accent, listOf(surface), contrast)

  private fun Colors.surfaces() = Surfaces(tableBackground, pageBackground, mobilePageBackground)

  private companion object {
    val BuiltIn = listOf(LightColors, DarkColors, MidnightColors)

    val Summary =
      CustomThemeSummary(
        name = "Test",
        repo = CustomThemeRepo(userName = "user", repoName = "test"),
        colors = emptyList(),
        mode = Dark,
      )
  }
}
