package aktual.prefs.ui

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.radioButton
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.vm.ListPreference
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Radio list of options, each row showing an optional [preview] of what that option produces, so
 * options can be compared at a glance instead of hiding behind a dropdown.
 */
@Composable
internal fun <T : Any> PreviewOptionGroup(
  title: String,
  preference: ListPreference<T>,
  label: @Composable (T) -> String,
  modifier: Modifier = Modifier,
  preview: (@Composable (T) -> Unit)? = null,
) {
  if (!preference.visible) return
  PreviewOptionGroup(
    title = title,
    options = preference.options,
    selected = preference.value,
    onSelect = preference.onChange,
    label = label,
    modifier = modifier,
    enabled = preference.enabled,
    preview = preview,
  )
}

@Composable
internal fun <T : Any> PreviewOptionGroup(
  title: String,
  options: ImmutableList<T>,
  selected: T,
  onSelect: (T) -> Unit,
  label: @Composable (T) -> String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  preview: (@Composable (T) -> Unit)? = null,
) {
  PreferenceGroup(title = title, modifier = modifier, enabled = enabled) {
    Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
      options.fastForEach { option ->
        PreviewOptionRow(
          label = label(option),
          isSelected = option == selected,
          enabled = enabled,
          onClick = { onSelect(option) },
          preview = preview?.let { p -> { p(option) } },
        )
      }
    }
  }
}

@Composable
private fun PreviewOptionRow(
  label: String,
  isSelected: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  preview: (@Composable () -> Unit)?,
) {
  val background by
    animateColorAsState(
      if (isSelected && enabled) colors.settingsNavItemBackgroundSelected else Transparent,
    )
  val contentColor =
    when {
      !enabled -> colors.pageTextSubdued
      isSelected -> colors.settingsNavItemTextSelected
      else -> colors.pageText
    }
  CompositionLocalProvider(LocalContentColor provides contentColor) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .heightIn(min = MinTouchHeight)
          .clip(CardShape)
          .background(background)
          .selectable(
            selected = isSelected,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick,
          )
          .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      preview?.invoke()
      Text(
        text = label,
        style = typography.bodyLarge,
        color = contentColor,
        fontWeight = if (isSelected) Medium else Normal,
        modifier = Modifier.weight(1f),
      )
      RadioButton(
        selected = isSelected,
        onClick = null,
        enabled = enabled,
        colors = colors.radioButton(),
      )
    }
  }
}

private val MinTouchHeight = 52.dp

@Preview
@Composable
private fun PreviewPreviewOptionGroup(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    PreviewOptionGroup(
      title = "Number format",
      options = persistentListOf("1,234.56", "1.234,56", "1 234,56"),
      selected = "1.234,56",
      onSelect = {},
      label = { it },
    )
  }
