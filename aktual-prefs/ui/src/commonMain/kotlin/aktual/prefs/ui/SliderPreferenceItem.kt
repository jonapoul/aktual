package aktual.prefs.ui

import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.disabled
import aktual.core.ui.slider
import aktual.prefs.vm.SliderPreference
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun SliderPreferenceItem(
  preference: SliderPreference,
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  modifier: Modifier = Modifier,
  includeBackground: Boolean = true,
  theme: Theme = LocalTheme.current,
) {
  SliderPreferenceItem(
    value = preference.value,
    range = preference.range,
    onValueChange = preference.onChange,
    enabled = preference.enabled,
    title = title,
    subtitle = subtitle,
    icon = icon,
    modifier = modifier,
    includeBackground = includeBackground,
    theme = theme,
  )
}

@Composable
internal fun SliderPreferenceItem(
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit,
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  includeBackground: Boolean = true,
  theme: Theme = LocalTheme.current,
) {
  var currentValue by remember(value) { mutableFloatStateOf(value) }
  BasicPreferenceItem(
    modifier = modifier,
    title = title,
    subtitle = subtitle,
    icon = icon,
    enabled = enabled,
    includeBackground = includeBackground,
    onClick = null,
    bottomContent = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Slider(
          modifier = Modifier.weight(1f),
          value = currentValue,
          onValueChange = { currentValue = it },
          valueRange = range,
          onValueChangeFinished = { onValueChange(currentValue) },
          enabled = enabled,
          colors = theme.slider(),
        )

        val measurer = rememberTextMeasurer()
        val style = LocalTextStyle.current
        val result =
          remember(measurer, style) { measurer.measure("%.1f".format(MEASURE_VALUE), style) }
        val width = with(LocalDensity.current) { result.size.width.toDp() }
        Text(
          modifier = Modifier.padding(horizontal = 8.dp).width(width),
          text = remember(currentValue) { "%.1f".format(currentValue) },
          textAlign = TextAlign.End,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = style,
          color = if (enabled) theme.pageText else theme.pageText.disabled,
        )
      }
    },
  )
}

private const val MEASURE_VALUE = 100f

@Preview
@Composable
private fun PreviewSliderPreferenceItem(
  @PreviewParameter(SliderPreferenceItemProvider::class)
  params: ThemedParams<SliderPreferenceItemParams>
) =
  PreviewWithColorScheme(params.theme) {
    SliderPreferenceItem(
      value = params.data.value,
      range = params.data.range,
      onValueChange = {},
      title = params.data.title,
      subtitle = params.data.subtitle,
      icon = params.data.icon,
      enabled = params.data.enabled,
    )
  }

private data class SliderPreferenceItemParams(
  val value: Float,
  val range: ClosedFloatingPointRange<Float>,
  val title: String,
  val subtitle: String?,
  val icon: ImageVector?,
  val enabled: Boolean = true,
)

private class SliderPreferenceItemProvider :
  ThemedParameterProvider<SliderPreferenceItemParams>(
    SliderPreferenceItemParams(
      value = 0f,
      range = 0f..100f,
      title = "Change the doodad",
      subtitle =
        "When you change this setting, the doodad will update. This might also affect the thingybob.",
      icon = MaterialIcons.Info,
    ),
    SliderPreferenceItemParams(
      value = 50f,
      range = 0f..100f,
      title = "This one has no subtitle and no icon",
      subtitle = null,
      icon = null,
    ),
    SliderPreferenceItemParams(
      value = 100f,
      range = 0f..100f,
      title = "Disabled preference",
      subtitle = "This item is disabled and should appear subdued",
      icon = MaterialIcons.Info,
      enabled = false,
    ),
  )
