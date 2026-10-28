package aktual.budget.transactions.ui

import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.vm.AccountOption
import aktual.budget.transactions.vm.AccountViewOptions
import aktual.core.l10n.Strings
import aktual.core.theme.hasAlternateRowColour
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.disabledIf
import aktual.core.ui.switch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableList

private val Densities = TransactionsDensity.entries.toImmutableList()

// The global options apply to every list, the account's to its own list only
@Immutable
internal data class ViewOptions(
  val density: TransactionsDensity,
  val alternateRowColours: Boolean = false,
  val expandSplits: Boolean = false,
  val account: AccountViewOptions? = null,
)

@Composable
internal fun ViewOptionsSheet(
  options: ViewOptions,
  onAction: ActionListener,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
  ) {
    ViewOptionsContent(options = options, onAction = onAction)
  }
}

@Composable
private fun ViewOptionsContent(
  options: ViewOptions,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp)
        .padding(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(text = Strings.transactionsViewOptions, style = typography.headlineSmall)

    AktualSlidingToggleButton(
      selected = options.density,
      options = Densities,
      onSelect = { onAction(Action.SetDensity(it)) },
      string = { it.string() },
      fontSize = 14.sp,
    )

    Column {
      // Only themes with their own alternate row colour shade rows, see hasAlternateRowColour
      SwitchRow(
        text = Strings.transactionsViewAlternateRows,
        subtitle =
          if (colors.hasAlternateRowColour) {
            null
          } else {
            Strings.transactionsViewAlternateRowsUnsupported
          },
        checked = options.alternateRowColours,
        onCheckedChange = { onAction(Action.SetAlternateRowColours(it)) },
        enabled = colors.hasAlternateRowColour,
      )

      SwitchRow(
        text = Strings.transactionsViewExpandSplits,
        checked = options.expandSplits,
        onCheckedChange = { onAction(Action.SetExpandSplits(it)) },
      )
    }

    val account = options.account
    if (account != null) {
      Column(
        modifier =
          Modifier.clip(CardShape).background(colors.cardBackground, CardShape).padding(12.dp),
      ) {
        Text(
          text = Strings.transactionsViewThisAccount,
          fontSize = 13.sp,
          color = colors.pageTextLight,
        )

        AccountSwitchRow(
          text = Strings.transactionsViewRunningBalance,
          checked = account.showRunningBalance,
          option = RunningBalance,
          onAction = onAction,
        )

        AccountSwitchRow(
          text = Strings.transactionsViewHideReconciled,
          checked = account.hideReconciled,
          option = HideReconciledRows,
          onAction = onAction,
        )

        AccountSwitchRow(
          text = Strings.transactionsViewExtraBalances,
          checked = account.showExtraBalances,
          option = ExtraBalances,
          onAction = onAction,
        )
      }
    }
  }

@Composable
private fun AccountSwitchRow(
  text: String,
  checked: Boolean,
  option: AccountOption,
  onAction: ActionListener,
) =
  SwitchRow(
    text = text,
    checked = checked,
    onCheckedChange = { onAction(Action.SetAccountOption(option, it)) },
  )

// The whole row toggles the switch
@Composable
private fun SwitchRow(
  text: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  enabled: Boolean = true,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = typography.bodyLarge,
        color = colors.pageTextLight.disabledIf(!enabled),
      )
      if (subtitle != null) {
        Text(text = subtitle, fontSize = 12.sp, color = colors.pageTextSubdued.disabledIf(!enabled))
      }
    }

    Switch(checked = checked, onCheckedChange = null, colors = colors.switch(), enabled = enabled)
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
  @PreviewParameter(ViewOptionsProvider::class) params: ColorParams<ViewOptions>,
) = PreviewWithColorParams(params) { ViewOptionsContent(options = this, onAction = {}) }

private class ViewOptionsProvider :
  ColorParamsProvider<ViewOptions>(
    ViewOptions(Comfortable),
    ViewOptions(
      density = Compact,
      alternateRowColours = true,
      account = AccountViewOptions(true, hideReconciled = false, showExtraBalances = true),
    ),
    ViewOptions(Dense, expandSplits = true),
  )
