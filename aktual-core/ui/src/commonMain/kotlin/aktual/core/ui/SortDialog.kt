package aktual.core.ui

import aktual.budget.model.SortDirection
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun <F> SortDialog(
  fields: ImmutableList<F>,
  field: F,
  direction: SortDirection,
  fieldLabel: @Composable (F) -> String,
  onConfirm: (F, SortDirection) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedField by remember { mutableStateOf(field) }
  var selectedDirection by remember { mutableStateOf(direction) }

  AktualAlertDialog(
    modifier = modifier,
    title = Strings.sort,
    onDismissRequest = onDismiss,
    buttons = {
      NormalTextButton(
        text = Strings.sortApply,
        onClick = { onConfirm(selectedField, selectedDirection) },
      )
    },
  ) {
    SortSection(
      label = Strings.sortField,
      options = fields,
      selected = selectedField,
      name = fieldLabel,
      onSelect = { selectedField = it },
    )

    VerticalSpacer(10.dp)

    SortSection(
      label = Strings.sortDirection,
      options = SortDirection.Entries,
      selected = selectedDirection,
      name = { it.label() },
      onSelect = { selectedDirection = it },
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
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    RadioButton(selected = isSelected, onClick = null, colors = colors.radioButton())
    Text(text = name, style = typography.bodyLarge, color = colors.pageText)
  }
}

@Composable
private fun SortDirection.label(): String =
  when (this) {
    Ascending -> Strings.sortDirectionAscending
    Descending -> Strings.sortDirectionDescending
  }

@Preview
@Composable
private fun PreviewSortDialog(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    SortDialog(
      fields = persistentListOf("Name", "Date"),
      field = "Name",
      direction = Ascending,
      fieldLabel = { it },
      onConfirm = { _, _ -> },
      onDismiss = {},
    )
  }
