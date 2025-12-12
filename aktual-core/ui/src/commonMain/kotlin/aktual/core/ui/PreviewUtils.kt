@file:Suppress("PreviewAnnotationNaming")

package aktual.core.ui

import aktual.core.model.ColorSchemeType
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
)
annotation class PortraitPreview

@Preview(
  name = "Landscape",
  showBackground = true,
  uiMode = AndroidUiModes.UI_MODE_NIGHT_UNDEFINED,
  widthDp = MY_PHONE_HEIGHT_DP,
  heightDp = MY_PHONE_WIDTH_DP,
)
annotation class LandscapePreview

@Preview(
  name = "Desktop",
  showBackground = true,
  widthDp = MY_MONITOR_WIDTH_DP,
  heightDp = MY_MONITOR_HEIGHT_DP,
)
annotation class DesktopPreview

@Preview(
  name = "Tablet",
  showBackground = true,
  device = Devices.PIXEL_TABLET,
)
annotation class TabletPreview

const val MY_PHONE_WIDTH_DP = 540 // 1080px * 160 / 400dpi
const val MY_PHONE_HEIGHT_DP = 1140 // 2280px * 160 / 400dpi

const val MY_MONITOR_HEIGHT_DP = 1920 // 1440px * 160 / 111dpi
const val MY_MONITOR_WIDTH_DP = 3413 // 2560px × 160 / 111dpi

open class PreviewParameters<T>(protected val data: List<T>) : PreviewParameterProvider<T> {
  private var labels = listOf<String>()
  override val values: Sequence<T> get() = data.asSequence()

  constructor(vararg values: T) : this(values.toList())

  constructor(vararg values: Pair<String, T>) : this(values.map { it.second }) {
    labels = values.map { it.first }
  }

  override fun getDisplayName(index: Int): String? = labels.getOrNull(index)
    ?: data[index]?.toString()
}

data class ThemedParams<T>(val type: ColorSchemeType, val data: T)

open class ThemedParameterProvider<T>(collection: List<T>) : PreviewParameterProvider<ThemedParams<T>> {
  private val all: List<ThemedParams<T>> = collection
    .flatMap { data -> ColorSchemeType.entries.map { type -> ThemedParams(type, data) } }

  override val values: Sequence<ThemedParams<T>> get() = all.asSequence()

  constructor(vararg values: T) : this(values.toList())

  override fun getDisplayName(index: Int): String? = all
    .getOrNull(index)
    ?.let { params -> "${params.type} - ${params.data}" }
}

class ColorSchemeParameters : PreviewParameters<ColorSchemeType>(ColorSchemeType.entries)

class ThemedBooleanParameters : ThemedParameterProvider<Boolean>(true, false)

@Composable
fun PreviewWithColorScheme(
  schemeType: ColorSchemeType,
  modifier: Modifier = Modifier,
  isPrivacyEnabled: Boolean = false,
  content: @Composable (ColorSchemeType) -> Unit,
) = WithCompositionLocals(
  isPrivacyEnabled = isPrivacyEnabled,
) {
  AktualTheme(schemeType) {
    Surface(modifier = modifier) {
      content(schemeType)
    }
  }
}
