package aktual.prefs.ui

import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.ListBottomSheet
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.textField
import aktual.prefs.vm.ListPreference
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun <E : Enum<E>> ListPreferenceItem(
  preference: ListPreference<E>,
  optionString: @Composable (E) -> String,
  optionSuffix: @Composable ((E) -> Unit)?,
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  modifier: Modifier = Modifier,
  includeBackground: Boolean = true,
) {
  ListPreferenceItem(
    value = preference.value,
    options = preference.options,
    enabled = preference.enabled,
    onValueChange = preference.onChange,
    optionString = optionString,
    optionSuffix = optionSuffix,
    title = title,
    subtitle = subtitle,
    icon = icon,
    modifier = modifier,
    includeBackground = includeBackground,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <E : Enum<E>> ListPreferenceItem(
  value: E,
  options: ImmutableList<E>,
  optionString: @Composable (E) -> String,
  optionSuffix: @Composable ((E) -> Unit)?,
  onValueChange: (E) -> Unit,
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  includeBackground: Boolean = true,
) {
  var showSheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState()

  BasicPreferenceItem(
    modifier = modifier,
    title = title,
    subtitle = subtitle,
    icon = icon,
    enabled = enabled,
    onClick = null,
    includeBackground = includeBackground,
    bottomContent = {
      val displayText = optionString(value)
      val textState = rememberTextFieldState(initialText = displayText)
      LaunchedEffect(displayText) {
        if (textState.text.toString() != displayText) {
          textState.edit { replace(0, length, displayText) }
        }
      }
      Box {
        AktualTextField(
          state = textState,
          modifier = Modifier.fillMaxWidth(),
          placeholderText = null,
          readOnly = true,
          isEnabled = enabled,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
          leadingIcon = optionSuffix?.let { suffix -> { suffix(value) } },
          colors =
            colors.textField(
              focusedContainer = colors.buttonNormalBackground,
              border = colors.buttonNormalBorder,
            ),
        )
        Box(modifier = Modifier.matchParentSize().clickable(enabled) { showSheet = true })
      }
    },
  )

  if (showSheet) {
    ListBottomSheet(
      value = value,
      options = options,
      onDismiss = { showSheet = false },
      onSelect = onValueChange,
      sheetState = sheetState,
      string = optionString,
      trailingContent = optionSuffix,
      key = { it.ordinal },
    )
  }
}

private enum class PreviewOption {
  OptionA,
  OptionB,
  OptionC,
}

@Preview
@Composable
private fun PreviewListPreferenceItem(
  @PreviewParameter(ListPreferenceItemProvider::class)
  params: ColoredParams<ListPreferenceItemParams>
) =
  PreviewWithColors(params.colors) {
    ListPreferenceItem(
      value = params.data.value,
      options = params.data.options,
      optionString = { it.name },
      optionSuffix = null,
      onValueChange = {},
      title = params.data.title,
      subtitle = params.data.subtitle,
      icon = params.data.icon,
      enabled = params.data.enabled,
    )
  }

private data class ListPreferenceItemParams(
  val value: PreviewOption = PreviewOption.OptionA,
  val options: ImmutableList<PreviewOption> = PreviewOption.entries.toImmutableList(),
  val title: String,
  val subtitle: String?,
  val icon: ImageVector?,
  val enabled: Boolean = true,
)

private class ListPreferenceItemProvider :
  ColoredParameterProvider<ListPreferenceItemParams>(
    ListPreferenceItemParams(
      title = "Change the doodad",
      subtitle =
        "When you change this setting, the doodad will update. This might also affect the thingybob.",
      icon = MaterialIcons.Info,
    ),
    ListPreferenceItemParams(title = "No subtitle or icon", subtitle = null, icon = null),
    ListPreferenceItemParams(
      title = "Disabled preference",
      subtitle = "This item is disabled and should appear subdued",
      icon = MaterialIcons.Info,
      enabled = false,
    ),
  )
