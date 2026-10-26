package aktual.budget.payees.ui.list

import aktual.core.l10n.Strings
import aktual.core.ui.BooleanColorParameters
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParams
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun UnusedPayeesChip(
  numUnused: Int,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Chip(
    modifier = modifier.semantics { selected = isSelected },
    text = Strings.payeesUnusedFilter(numUnused),
    colors = ChipDefaults.selectableColors(isSelected),
    onClick = onClick,
  )
}

@Preview
@Composable
private fun PreviewUnusedPayeesChip(
  @PreviewParameter(BooleanColorParameters::class) params: ColorParams<Boolean>,
) =
  PreviewWithColorParams(params) {
    UnusedPayeesChip(numUnused = 3, isSelected = this, onClick = {})
  }
