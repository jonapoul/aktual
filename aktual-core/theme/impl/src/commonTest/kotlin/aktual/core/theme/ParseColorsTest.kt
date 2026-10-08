package aktual.core.theme

import aktual.test.ThemeResponses
import androidx.compose.ui.graphics.Color
import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test

class ParseColorsTest {
  @Test
  fun `Parse CSS as theme`() {
    assertThat(parseColors(ShadesOfCoffeeThemeSummary, ThemeResponses.CUSTOM_THEME_200))
      .isDataClassEqualTo(ShadesOfCoffeeTheme)
  }

  @Test
  fun `Resolve var references`() {
    val css =
      """
      :root {
        --color-pageBackground: #1b1c1d;
        --color-tableText: #e9eaeb;
        --color-numberNegative: #b85261;
        --color-budgetNumberNegative: var(--color-numberNegative);
        --color-budgetNumberNeutral: var(--color-tableText);
        --color-budgetNumberPositive: var(--color-budgetNumberNeutral);
        --color-toBudgetNegative: var(--color-budgetNumberNegative);
        --color-toBudgetZero: var(--color-unknown);
        --color-toBudgetPositive: var(--color-unknown, #94b852);
        --color-reportsRed: var(--color-reportsBlue);
        --color-reportsBlue: var(--color-reportsRed);
      }
      """
        .trimIndent()

    val colors = parseColors(ThyriumThemeSummary, css) as JsonCustomColors

    assertThat(colors.misc).all {
      prop(MiscColors::budgetNumberNegative).isEqualTo(Color(0xFFb85261))
      prop(MiscColors::budgetNumberNeutral).isEqualTo(Color(0xFFe9eaeb))
      prop(MiscColors::budgetNumberPositive).isEqualTo(Color(0xFFe9eaeb))
      prop(MiscColors::toBudgetNegative).isEqualTo(Color(0xFFb85261))
      prop(MiscColors::toBudgetZero).isEqualTo(DarkColors.toBudgetZero)
      prop(MiscColors::toBudgetPositive).isEqualTo(Color(0xFF94b852))
      prop(MiscColors::reportsRed).isEqualTo(DarkColors.reportsRed)
      prop(MiscColors::reportsBlue).isEqualTo(DarkColors.reportsBlue)
    }
  }

  @Test
  fun `Resolve var references to the theme's own properties`() {
    val css =
      """
      :root {
        --font-family: "Hasklig";
        --dracula-page-background: #22242f;
        --dracula-foreground: #f8f8f2;
        --dracula-red: #ff5555;
        --dracula-purple-muted: rgba(189, 147, 249, 0.14);
        --dracula-accent: var(--dracula-red);

        --color-pageBackground: var(--dracula-page-background);
        --color-pageText: var(--dracula-foreground);
        --color-numberNegative: var(--dracula-accent);
        --color-cardBackground: var(--dracula-purple-muted);
        --color-reportsLabel: var(--color-pageText);
        --color-reportsRed: var(--dracula-unknown);
      }
      """
        .trimIndent()

    val colors = parseColors(ThyriumThemeSummary, css) as JsonCustomColors

    assertThat(colors.page).all {
      prop(PageColors::pageBackground).isEqualTo(Color(0xFF22242f))
      prop(PageColors::pageText).isEqualTo(Color(0xFFf8f8f2))
      prop(PageColors::numberNegative).isEqualTo(Color(0xFFff5555))
      prop(PageColors::cardBackground).isEqualTo(Color(189, 147, 249).copy(alpha = 0.14f))
    }
    assertThat(colors.misc).all {
      prop(MiscColors::reportsLabel).isEqualTo(Color(0xFFf8f8f2))
      prop(MiscColors::reportsRed).isEqualTo(DarkColors.reportsRed)
    }
  }
}

private val ThyriumThemeSummary =
  CustomThemeSummary(
    name = "Thyrium",
    repo = CustomThemeRepo(userName = "carlisle96", repoName = "thyrium-actual"),
    colors = emptyList(),
    mode = Dark,
  )
