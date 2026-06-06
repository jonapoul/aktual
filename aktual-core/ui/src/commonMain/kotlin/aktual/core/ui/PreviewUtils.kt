@file:Suppress("PreviewAnnotationNaming")

package aktual.core.ui

import aktual.core.theme.Colors
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Preview(
  name = "Portrait",
  showBackground = true,
  uiMode = AndroidUiModes.UI_MODE_NIGHT_UNDEFINED,
  widthDp = MY_PHONE_WIDTH_DP,
  heightDp = MY_PHONE_HEIGHT_DP,
  locale = "en",
)
annotation class PortraitPreview

@Preview(
  name = "Landscape",
  showBackground = true,
  uiMode = AndroidUiModes.UI_MODE_NIGHT_UNDEFINED,
  widthDp = MY_PHONE_HEIGHT_DP,
  heightDp = MY_PHONE_WIDTH_DP,
  locale = "en",
)
annotation class LandscapePreview

@Preview(
  name = "Desktop",
  showBackground = true,
  widthDp = MY_MONITOR_WIDTH_DP,
  heightDp = MY_MONITOR_HEIGHT_DP,
  locale = "en",
)
annotation class DesktopPreview

@Preview(name = "Tablet", showBackground = true, device = Devices.PIXEL_TABLET, locale = "en")
annotation class TabletPreview

const val MY_PHONE_WIDTH_DP = 540 // 1080px * 160 / 400dpi
const val MY_PHONE_HEIGHT_DP = 1140 // 2280px * 160 / 400dpi

const val MY_MONITOR_HEIGHT_DP = 1920 // 1440px * 160 / 111dpi
const val MY_MONITOR_WIDTH_DP = 3413 // 2560px × 160 / 111dpi

open class PreviewParameters<T>(protected val data: List<T>) : PreviewParameterProvider<T> {
  private var labels = emptyList<String>()
  override val values: Sequence<T>
    get() = data.asSequence()

  constructor(vararg values: T) : this(values.toList())

  constructor(vararg values: Pair<String, T>) : this(values.map { it.second }) {
    labels = values.map { it.first }
  }

  override fun getDisplayName(index: Int): String? =
    labels.getOrNull(index) ?: data[index]?.toString()
}

@Immutable data class ColoredParams<T>(val colors: Colors, val data: T)

open class ColoredParameterProvider<T>(collection: List<T>) :
  PreviewParameterProvider<ColoredParams<T>> {
  private val all: List<ColoredParams<T>> = collection.flatMap { data ->
    Colors.Defaults.map { type -> ColoredParams(type, data) }
  }

  override val values: Sequence<ColoredParams<T>>
    get() = all.asSequence()

  constructor(vararg values: T) : this(values.toList())

  override fun getDisplayName(index: Int): String? =
    all.getOrNull(index)?.let { params -> "${params.colors::class.simpleName} - ${params.data}" }
}

class ColoredParameters : PreviewParameters<Colors>(LightColors, DarkColors, MidnightColors)

class ColoredBooleanParameters : ColoredParameterProvider<Boolean>(true, false)

@Composable
fun PreviewWithColors(
  colors: Colors,
  modifier: Modifier = Modifier,
  isPrivacyEnabled: Boolean = false,
  content: @Composable () -> Unit,
) =
  WithCompositionLocals(isPrivacyEnabled = isPrivacyEnabled) {
    AktualTheme(colors) {
      Surface(modifier = modifier, color = colors.pageBackground, content = content)
    }
  }

@Composable
fun <T> PreviewWithColoredParams(
  params: ColoredParams<T>,
  modifier: Modifier = Modifier,
  isPrivacyEnabled: Boolean = false,
  content: @Composable T.() -> Unit,
) {
  WithCompositionLocals(isPrivacyEnabled = isPrivacyEnabled) {
    AktualTheme(params.colors) {
      Surface(modifier = modifier, color = params.colors.pageBackground) {
        with(params.data) { content() }
      }
    }
  }
}
