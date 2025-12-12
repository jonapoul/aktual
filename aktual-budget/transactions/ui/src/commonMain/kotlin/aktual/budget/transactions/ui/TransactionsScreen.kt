package aktual.budget.transactions.ui

import aktual.budget.model.AccountSpec
import aktual.budget.model.BudgetId
import aktual.budget.model.TransactionsFormat
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.LoadedAccount
import aktual.budget.transactions.vm.TransactionIdSource
import aktual.budget.transactions.vm.TransactionStateSource
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.model.ColorSchemeType
import aktual.core.model.LoginToken
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.LandscapePreview
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TabletPreview
import aktual.core.ui.WavyBackground
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun TransactionsScreen(
  nav: TransactionsNavigator,
  budgetId: BudgetId,
  token: LoginToken,
  spec: TransactionsSpec = TransactionsSpec(AccountSpec.AllAccounts),
  viewModel: TransactionsViewModel = metroViewModel(token, budgetId, spec),
) {
  val loadedAccount by viewModel.loadedAccount.collectAsStateWithLifecycle()
  val format by viewModel.format.collectAsStateWithLifecycle()

  TransactionsScaffold(
    transactionIdSource = viewModel,
    loadedAccount = loadedAccount,
    format = format,
    source = viewModel,
    onAction = { action ->
      when (action) {
        Action.NavBack -> nav.back()
        is Action.CheckItem -> viewModel.setChecked(action.id, action.isChecked)
        is Action.SetPrivacyMode -> viewModel.setPrivacyMode(action.isPrivacyEnabled)
      }
    },
  )
}

@Composable
private fun metroViewModel(
  token: LoginToken,
  budgetId: BudgetId,
  spec: TransactionsSpec,
) = assistedMetroViewModel<TransactionsViewModel, TransactionsViewModel.Factory> {
  create(token, budgetId, spec)
}

@Composable
internal fun TransactionsScaffold(
  transactionIdSource: TransactionIdSource,
  loadedAccount: LoadedAccount,
  format: TransactionsFormat,
  source: TransactionStateSource,
  onAction: ActionListener,
) {
  val theme = LocalTheme.current
  Scaffold(
    topBar = { TransactionsTitleBar(loadedAccount, onAction, theme) },
  ) { innerPadding ->
    Box {
      WavyBackground()

      Transactions(
        modifier = Modifier.padding(innerPadding),
        transactionIdSource = transactionIdSource,
        format = format,
        source = source,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}


@Composable
@PortraitPreview
@LandscapePreview
@TabletPreview
private fun PreviewTransactionsScaffold(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  TransactionsScaffold(
    transactionIdSource = PreviewTransactionIdSource(listOf(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3)),
    loadedAccount = LoadedAccount.AllAccounts,
    format = TransactionsFormat.Table,
    source = previewTransactionStateSource(
      TRANSACTION_1 to false,
      TRANSACTION_2 to true,
      TRANSACTION_3 to false,
    ),
    onAction = {},
  )
}
