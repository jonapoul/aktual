package aktual.budget.tags.ui.list

import aktual.budget.model.TagSort
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColoredParameters
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.radioButton
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun TagSortDialog(
  sort: TagSort,
  onConfirm: (TagSort) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var field by remember { mutableStateOf(sort.field) }
  var direction by remember { mutableStateOf(sort.direction) }

  AktualAlertDialog(
    modifier = modifier,
    title = Strings.tagsSort,
    onDismissRequest = onDismiss,
    buttons = {
      NormalTextButton(
        text = Strings.tagsSortApply,
        onClick = { onConfirm(TagSort(field, direction)) },
      )
    },
  ) {
    SortSection(
      label = Strings.tagsSortField,
      options = TagSort.Field.Entries,
      selected = field,
      name = { it.label() },
      onSelect = { field = it },
    )

    VerticalSpacer(10.dp)

    SortSection(
      label = Strings.tagsSortDirection,
      options = TagSort.Direction.Entries,
      selected = direction,
      name = { it.label() },
      onSelect = { direction = it },
    )
  }
}

@Composable
private fun <T> ColumnScope.SortSection(
  label: String,
  options: ImmutableList<T>,
  selected: T,
  name: @Composable (T) -> String,
  onSelect: (T) -> Unit,
) {
  Text(
    modifier = Modifier.fillMaxWidth(),
    text = label,
    style = typography.labelLarge,
    color = colors.pageTextSubdued,
  )

  Column(Modifier.fillMaxWidth().selectableGroup()) {
    for (option in options) {
      SortOptionRow(
        isSelected = option == selected,
        onClick = { onSelect(option) },
        name = name(option),
      )
    }
  }
}

@Composable
private fun SortOptionRow(isSelected: Boolean, onClick: () -> Unit, name: String) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .selectable(selected = isSelected, onClick = onClick, role = null)
        .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    RadioButton(selected = isSelected, onClick = null, colors = colors.radioButton())
    Text(text = name, style = typography.bodyLarge, color = colors.pageText)
  }
}

@Composable
private fun TagSort.Field.label(): String =
  when (this) {
    TagSort.Field.Name -> Strings.tagsSortFieldName
    TagSort.Field.Usage -> Strings.tagsSortFieldUsage
  }

@Composable
private fun TagSort.Direction.label(): String =
  when (this) {
    TagSort.Direction.Ascending -> Strings.tagsSortDirectionAscending
    TagSort.Direction.Descending -> Strings.tagsSortDirectionDescending
  }

@Preview
@Composable
private fun PreviewTagSortDialog(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    TagSortDialog(sort = TagSort.Default, onConfirm = {}, onDismiss = {})
  }
