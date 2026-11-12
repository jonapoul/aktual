package aktual.core.ui

import aktual.budget.model.SortDirection
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.typography
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// Applies each choice straight away, unlike SortDialog
@Composable
fun <F> SortSheet(
  fields: ImmutableList<F>,
  field: F,
  direction: SortDirection,
  fieldLabel: @Composable (F) -> String,
  onChange: (F, SortDirection) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
    SortSheetContent(fields, field, direction, fieldLabel, onChange)
  }
}

@Composable
private fun <F> SortSheetContent(
  fields: ImmutableList<F>,
  field: F,
  direction: SortDirection,
  fieldLabel: @Composable (F) -> String,
  onChange: (F, SortDirection) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 20.dp)) {
    Text(text = Strings.sort, style = typography.headlineSmall)

    VerticalSpacer(12.dp)

    SortSection(
      label = Strings.sortField,
      options = fields,
      selected = field,
      name = fieldLabel,
      onSelect = { onChange(it, direction) },
    )

    VerticalSpacer(10.dp)

    SortSection(
      label = Strings.sortDirection,
      options = SortDirection.Entries,
      selected = direction,
      name = { it.label() },
      onSelect = { onChange(field, it) },
    )
  }

@Preview
@Composable
private fun PreviewSortSheet(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    SortSheetContent(
      fields = persistentListOf("Name", "Date"),
      field = "Name",
      direction = Descending,
      fieldLabel = { it },
      onChange = { _, _ -> },
    )
  }
