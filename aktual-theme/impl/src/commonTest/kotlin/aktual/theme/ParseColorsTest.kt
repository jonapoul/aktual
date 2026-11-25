package aktual.theme

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
  fun `Colours a theme leaves out follow its accent`() {
    val css =
      """
      :root {
        --color-pageBackground: #1b1c1d;
        --color-accent: #2b8fed;
        --color-accentText: #66b5fa;
      }
      """
        .trimIndent()

    val colors = parseColors(ThyriumThemeSummary, css)

    assertThat(colors).all {
      prop(CustomColors::accent).isEqualTo(Color(0xFF2b8fed))
      prop(CustomColors::buttonPrimaryBackground).isEqualTo(Color(0xFF2b8fed))
      prop(CustomColors::checkboxBackgroundSelected).isEqualTo(Color(0xFF2b8fed))
      prop(CustomColors::calendarSelectedBackground).isEqualTo(Color(0xFF2b8fed))
      prop(CustomColors::pageTextLink).isEqualTo(Color(0xFF66b5fa))
      prop(CustomColors::accentHover).isEqualTo(DarkColors.accentHover)
    }
  }

  @Test
  fun `Take the accent from older themes' colours`() {
    val css =
      """
      :root {
        --color-pageBackground: #1b1c1d;
        --color-buttonPrimaryBackground: #2b8fed;
        --color-pageTextLink: #66b5fa;
      }
      """
        .trimIndent()

    val colors = parseColors(ThyriumThemeSummary, css)

    assertThat(colors).all {
      prop(CustomColors::accent).isEqualTo(Color(0xFF2b8fed))
      prop(CustomColors::accentText).isEqualTo(Color(0xFF66b5fa))
      prop(CustomColors::checkboxBackgroundSelected).isEqualTo(Color(0xFF2b8fed))
      prop(CustomColors::menuItemTextSelected).isEqualTo(Color(0xFF66b5fa))
    }
  }

  @Test
  fun `Ignore comments`() {
    val css =
      """
      /* The palette lives in the --tn-* variables, and every
         --color-* variable points to one of them. */
      :root {
        --tn-bg: #1a1b26;
        /* --color-pageText: #ffffff; */
        --color-pageBackground: var(--tn-bg); /* page */
        --color-pageText: #c0caf5;
      }
      """
        .trimIndent()

    val colors = parseColors(ThyriumThemeSummary, css) as JsonCustomColors

    assertThat(colors.page).all {
      prop(PageColors::pageBackground).isEqualTo(Color(0xFF1a1b26))
      prop(PageColors::pageText).isEqualTo(Color(0xFFc0caf5))
    }
  }

  @Test
  fun `Parse minified CSS`() {
    val css = ":root{--color-pageBackground:#1a1b26;--color-pageText:#c0caf5}"

    val colors = parseColors(ThyriumThemeSummary, css) as JsonCustomColors

    assertThat(colors.page).all {
      prop(PageColors::pageBackground).isEqualTo(Color(0xFF1a1b26))
      prop(PageColors::pageText).isEqualTo(Color(0xFFc0caf5))
    }
  }

  @Test
  fun `Parse declarations sharing a line or spanning several`() {
    val css =
      """
      :root { --color-pageBackground: #1a1b26;
        --color-pageText: #c0caf5; --color-numberNegative: #f7768e;
        --color-cardBackground: rgba(
          26, 27, 38, 0.5
        );
        --color-pageTextLight: #565f89
      }
      """
        .trimIndent()

    val colors = parseColors(ThyriumThemeSummary, css) as JsonCustomColors

    assertThat(colors.page).all {
      prop(PageColors::pageBackground).isEqualTo(Color(0xFF1a1b26))
      prop(PageColors::pageText).isEqualTo(Color(0xFFc0caf5))
      prop(PageColors::numberNegative).isEqualTo(Color(0xFFf7768e))
      prop(PageColors::cardBackground).isEqualTo(Color(26, 27, 38).copy(alpha = 0.5f))
      prop(PageColors::pageTextLight).isEqualTo(Color(0xFF565f89))
    }
  }

  @Test
  fun `Fall back for colors that can't be parsed`() {
    val css =
      """
      :root {
        --color-pageBackground: #1a1b26;
        --color-pageText: inherit;
        --color-numberNegative: #f7768e;
      }
      """
        .trimIndent()

    val colors = parseColors(ThyriumThemeSummary, css) as JsonCustomColors

    assertThat(colors.page).all {
      prop(PageColors::pageText).isEqualTo(DarkColors.pageText)
      prop(PageColors::numberNegative).isEqualTo(Color(0xFFf7768e))
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
