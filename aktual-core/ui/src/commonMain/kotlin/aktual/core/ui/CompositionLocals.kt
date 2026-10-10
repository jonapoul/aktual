@file:Suppress("ComposeCompositionLocalUsage")

package aktual.core.ui

import aktual.budget.model.Amount
import aktual.budget.model.Currency
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.NumberFormat
import aktual.budget.model.NumberFormatConfig
import aktual.core.l10n.Res
import aktual.core.l10n.redacted_script
import aktual.core.theme.BottomBarThemeAttrs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat
import org.jetbrains.compose.resources.Font

val LocalPrivacyEnabled = compositionLocalOf { false }

// Opens the budget nav drawer, or null when the current layout has no drawer
val LocalNavDrawerOpener = compositionLocalOf<(() -> Unit)?> { null }

// Content drawn at the app root, above the bottom status bar
@Stable
class RootOverlay {
  var content: (@Composable () -> Unit)? by mutableStateOf(null)
}

val LocalRootOverlay =
  staticCompositionLocalOf<RootOverlay> { error("No RootOverlay value provided") }

internal val DefaultBottomBarThemeAttrs =
  BottomBarThemeAttrs(
    shouldHazeOnRootLevel = true,
    background = { cardBackground },
    foreground = { pageText },
  )

@Stable
class BottomBarThemeAttrsStack {
  private val overrides = mutableStateListOf<BottomBarThemeAttrs>()

  val current: BottomBarThemeAttrs by derivedStateOf {
    overrides.lastOrNull() ?: DefaultBottomBarThemeAttrs
  }

  fun push(attrs: BottomBarThemeAttrs) {
    overrides.add(attrs)
  }

  fun pop(attrs: BottomBarThemeAttrs) {
    // remove from the top so nested push/pop pairs stay LIFO even when the same attrs
    // instance is pushed multiple times
    val index = overrides.lastIndexOf(attrs)
    if (index >= 0) overrides.removeAt(index)
  }
}

val LocalBottomBarThemeAttrs =
  staticCompositionLocalOf<BottomBarThemeAttrsStack> {
    error("No BottomBarThemeAttrsStack value provided")
  }

val LocalDateFormatter =
  compositionLocalOf<DateTimeFormat<LocalDate>> { error("No LocalDateFormat value provided") }

@Stable
@Composable
@ReadOnlyComposable
fun LocalDate.formatted(formatter: DateTimeFormat<LocalDate> = LocalDateFormatter.current): String =
  formatter.format(this)

val LocalNumberFormatConfig =
  compositionLocalOf<NumberFormatConfig> { error("No NumberFormatConfig value provided") }

val LocalCurrencyConfig =
  compositionLocalOf<CurrencyConfig> { error("No CurrencyConfig value provided") }

val LocalHazeState = compositionLocalOf<HazeState> { error("No HazeState value provided") }

val LocalHazeConfig = compositionLocalOf<HazeConfig> { error("No HazeConfig value provided") }

val LocalDialogBlurState = compositionLocalOf { DialogBlurState() }

/**
 * Tracks how many dialogs are currently showing, so the background blur can animate accordingly.
 */
@Stable
class DialogBlurState {
  var activeDialogCount by mutableIntStateOf(0)
    internal set

  val isActive: Boolean
    get() = activeDialogCount > 0

  // Bounds (in root coordinates) that should be punched out of the blur overlay.
  // Keyed by an identity token so multiple simultaneous anchors don't collide.
  internal val excludedFromBlur = mutableStateMapOf<Any, Rect>()
}

@Composable
@ReadOnlyComposable
fun Amount.formattedString(
  numberFormatConfig: NumberFormatConfig = LocalNumberFormatConfig.current,
  currencyConfig: CurrencyConfig = LocalCurrencyConfig.current,
  includeSign: Boolean = false,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
): String = toString(numberFormatConfig, currencyConfig, includeSign, isPrivacyEnabled)

// Same as formattedString, but draws the privacy mask as a scribble
@Composable
fun Amount.formattedText(
  numberFormatConfig: NumberFormatConfig = LocalNumberFormatConfig.current,
  currencyConfig: CurrencyConfig = LocalCurrencyConfig.current,
  includeSign: Boolean = false,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
): AnnotatedString =
  toString(numberFormatConfig, currencyConfig, includeSign, isPrivacyEnabled).redacted()

// Draws any privacy masks in the string as a scribble
@Composable fun String.redacted(): AnnotatedString = AnnotatedString(this).redacted()

@Composable
fun AnnotatedString.redacted(): AnnotatedString {
  if (Amount.PRIVACY_MASK !in text) return this
  val style = SpanStyle(fontFamily = redactedFontFamily())
  val masks =
    Regex.fromLiteral(Amount.PRIVACY_MASK).findAll(text).map { match ->
      AnnotatedString.Range(style, match.range.first, match.range.last + 1)
    }
  return AnnotatedString(
    text = text.replace(Amount.PRIVACY_MASK, REDACTED_MASK),
    annotations = spanStyles + paragraphStyles + getLinkAnnotations(0, length) + masks,
  )
}

@Composable fun redactedFontFamily(): FontFamily = FontFamily(Font(Res.font.redacted_script))

// The font only has scribbles for letters and digits. Same length as the mask it replaces, so
// existing spans still line up
const val REDACTED_MASK = "redac"

@Composable
fun WithCompositionLocals(
  isPrivacyEnabled: Boolean = false,
  format: NumberFormat = Default,
  hideFraction: Boolean = false,
  currency: Currency = Default,
  currencyPosition: CurrencySymbolPosition = Default,
  addCurrencySpace: Boolean = true,
  dateFormat: DateFormat = Default,
  hazeState: HazeState = rememberHazeState(),
  hazeConfig: HazeConfig = remember { HazeConfig() },
  dialogBlurState: DialogBlurState = remember { DialogBlurState() },
  bottomBarThemeAttrs: BottomBarThemeAttrsStack = remember { BottomBarThemeAttrsStack() },
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalDateFormatter provides dateFormat.formatter(),
    LocalNumberFormatConfig provides NumberFormatConfig(format, hideFraction),
    LocalCurrencyConfig provides CurrencyConfig(currency, currencyPosition, addCurrencySpace),
    LocalPrivacyEnabled provides isPrivacyEnabled,
    LocalHazeState provides hazeState,
    LocalHazeConfig provides hazeConfig,
    LocalDialogBlurState provides dialogBlurState,
    LocalBottomBarThemeAttrs provides bottomBarThemeAttrs,
    content = content,
  )
}
