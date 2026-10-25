package aktual.budget.payees.ui.list

import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColoredBooleanParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
  val shape = RoundedCornerShape(ListPayeesDS.filterChipCornerRadius)
  val background = if (isSelected) colors.pillBackgroundSelected else colors.pillBackground
  val border = if (isSelected) colors.pillBackgroundSelected else colors.pillBorder
  Box(
    modifier =
      modifier
        .semantics { selected = isSelected }
        .clip(shape)
        .background(background, shape)
        .border(ListPayeesDS.filterChipBorder, border, shape)
        .clickable(onClick = onClick)
        .heightIn(min = ListPayeesDS.filterChipMinHeight)
        .padding(ListPayeesDS.filterChipPadding),
    contentAlignment = Center,
  ) {
    Text(
      text = Strings.payeesUnusedFilter(numUnused),
      style = typography.labelLarge,
      color = if (isSelected) colors.pillTextSelected else colors.pillText,
      maxLines = 1,
    )
  }
}

@Preview
@Composable
private fun PreviewUnusedPayeesChip(
  @PreviewParameter(ColoredBooleanParameters::class) params: ColoredParams<Boolean>,
) =
  PreviewWithColoredParams(params) {
    UnusedPayeesChip(numUnused = 3, isSelected = this, onClick = {})
  }
