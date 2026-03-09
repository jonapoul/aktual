package aktual.settings.ui

import aktual.core.icons.Check
import aktual.core.icons.Info
import aktual.core.icons.MaterialIcons
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TextField
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.scrollbar
import aktual.core.ui.textField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <E : Enum<E>> ListPreferenceItem(
  value: E,
  options: ImmutableList<E>,
  optionString: @Composable (E) -> String,
  optionIcon: ((E) -> ImageVector)?,
  onValueChange: (E) -> Unit,
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  includeBackground: Boolean = true,
  theme: Theme = LocalTheme.current,
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
    theme = theme,
    bottomContent = {
      Box {
        TextField(
          modifier = Modifier.fillMaxWidth(),
          value = optionString(value),
          onValueChange = {},
          placeholderText = null,
          readOnly = true,
          enabled = enabled,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
          leadingIcon = optionIcon?.let { iconComposable -> { iconComposable(value) } },
          colors = theme.textField(),
        )
        Box(modifier = Modifier.matchParentSize().clickable(enabled) { showSheet = true })
      }
    },
  )

  if (showSheet) {
    ModalBottomSheet(
      onDismissRequest = { showSheet = false },
      sheetState = sheetState,
      containerColor = theme.pageBackground,
      contentColor = theme.pageText,
    ) {
      val listState = rememberLazyListState()
      LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
        items(options) { option ->
          val label = optionString(option)
          val isSelected = option == value
          ListItem(
            modifier =
              Modifier.clickable {
                onValueChange(option)
                showSheet = false
              },
            headlineContent = { Text(label) },
            leadingContent = optionIcon?.let { iconFn -> { Icon(iconFn(option), label) } },
            trailingContent =
              if (isSelected) {
                { Icon(MaterialIcons.Check, contentDescription = null) }
              } else {
                null
              },
            colors =
              ListItemDefaults.colors(
                containerColor = Color.Transparent,
                headlineColor = theme.pageText,
                leadingIconColor = theme.pageText,
                trailingIconColor = theme.pageText,
              ),
          )
        }
      }
    }
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
  params: ThemedParams<ListPreferenceItemParams>
) =
  PreviewWithColorScheme(params.theme) {
    ListPreferenceItem(
      value = params.data.value,
      options = params.data.options,
      optionString = { it.name },
      optionIcon = null,
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
  ThemedParameterProvider<ListPreferenceItemParams>(
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
