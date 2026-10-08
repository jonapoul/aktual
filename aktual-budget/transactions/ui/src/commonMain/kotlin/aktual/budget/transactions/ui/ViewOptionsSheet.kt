package aktual.budget.transactions.ui

import aktual.budget.model.TransactionsDensity
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableList

private val Densities = TransactionsDensity.entries.toImmutableList()

@Composable
internal fun ViewOptionsSheet(
  density: TransactionsDensity,
  onAction: ActionListener,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
  ) {
    ViewOptionsContent(density = density, onAction = onAction)
  }
}

@Composable
private fun ViewOptionsContent(
  density: TransactionsDensity,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(text = Strings.transactionsViewOptions, style = typography.headlineSmall)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(text = Strings.transactionsDensity, fontSize = 13.sp, color = colors.pageTextLight)

      AktualSlidingToggleButton(
        selected = density,
        options = Densities,
        onSelect = { onAction(Action.SetDensity(it)) },
        string = { it.string() },
        fontSize = 14.sp,
      )
    }
  }

@Composable
private fun TransactionsDensity.string(): String =
  when (this) {
    Comfortable -> Strings.transactionsDensityComfortable
    Compact -> Strings.transactionsDensityCompact
    Dense -> Strings.transactionsDensityDense
  }

@Preview
@Composable
private fun PreviewViewOptions(
  @PreviewParameter(ViewOptionsProvider::class) params: ColoredParams<TransactionsDensity>,
) = PreviewWithColoredParams(params) { ViewOptionsContent(density = this, onAction = {}) }

private class ViewOptionsProvider :
  ColoredParameterProvider<TransactionsDensity>(Comfortable, Compact, Dense)
