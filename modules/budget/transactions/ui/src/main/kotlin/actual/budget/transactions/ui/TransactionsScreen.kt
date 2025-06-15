package actual.budget.transactions.ui

import actual.account.model.LoginToken
import actual.budget.model.AccountSpec
import actual.budget.model.BudgetId
import actual.budget.model.SortColumn
import actual.budget.model.SortDirection
import actual.budget.model.TransactionId
import actual.budget.model.TransactionsFormat
import actual.budget.model.TransactionsSpec
import actual.budget.transactions.res.Strings
import actual.budget.transactions.vm.DatedTransactions
import actual.budget.transactions.vm.LoadedAccount
import actual.budget.transactions.vm.TransactionsSorting
import actual.budget.transactions.vm.TransactionsViewModel
import actual.core.res.CoreStrings
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Composable
fun TransactionsScreen(
  nav: TransactionsNavigator,
  budgetId: BudgetId,
  token: LoginToken,
  config: TransactionsSpec = TransactionsSpec(AccountSpec.AllAccounts),
  viewModel: TransactionsViewModel = hiltViewModel(token, budgetId, config),
) {
  val transactions by viewModel.transactions.collectAsStateWithLifecycle()
  val loadedAccount by viewModel.loadedAccount.collectAsStateWithLifecycle()
  val format by viewModel.format.collectAsStateWithLifecycle()
  val sorting by viewModel.sorting.collectAsStateWithLifecycle()

  val provider = remember {
    object : StateProvider {
      override fun isChecked(id: TransactionId): Flow<Boolean> = viewModel.isChecked(id)
      override fun isExpanded(date: LocalDate): Flow<Boolean> = viewModel.isExpanded(date)
    }
  }

  TransactionsScaffold(
    transactions = transactions,
    loadedAccount = loadedAccount,
    format = format,
    sorting = sorting,
    provider = provider,
    onAction = { action ->
      when (action) {
        Action.NavBack -> nav.back()
        is Action.CheckItem -> viewModel.setChecked(action.id, action.isChecked)
        is Action.ExpandGroup -> viewModel.setExpanded(action.group, action.isExpanded)
      }
    },
  )
}

@Composable
private fun hiltViewModel(
  token: LoginToken,
  budgetId: BudgetId,
  config: TransactionsSpec,
) = hiltViewModel<TransactionsViewModel, TransactionsViewModel.Factory>(
  creationCallback = { factory -> factory.create(TransactionsViewModel.Inputs(token, budgetId, config)) },
)

@Composable
private fun TransactionsScaffold(
  transactions: ImmutableList<DatedTransactions>,
  loadedAccount: LoadedAccount,
  format: TransactionsFormat,
  sorting: TransactionsSorting,
  provider: StateProvider,
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
        transactions = transactions,
        format = format,
        sorting = sorting,
        provider = provider,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Composable
private fun TransactionsTitleBar(
  loadedAccount: LoadedAccount,
  onAction: ActionListener,
  theme: Theme = LocalTheme.current,
) {
  val title = when (loadedAccount) {
    LoadedAccount.AllAccounts -> Strings.transactionsTitleAll
    LoadedAccount.Loading -> Strings.transactionsTitleLoading
    is LoadedAccount.SpecificAccount -> loadedAccount.account.name ?: Strings.transactionsTitleNone
  }

  TopAppBar(
    colors = theme.transparentTopAppBarColors(),
    navigationIcon = {
      IconButton(onClick = { onAction(Action.NavBack) }) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = CoreStrings.navBack,
        )
      }
    },
    title = {
      Text(
        text = title,
        maxLines = 1,
        overflow = Ellipsis,
      )
    },
  )
}

@Preview
@Composable
private fun TitleBarAll() = PreviewColumn {
  TransactionsTitleBar(
    loadedAccount = LoadedAccount.AllAccounts,
    onAction = {},
  )
}

@Preview
@Composable
private fun TitleBarLoading() = PreviewColumn {
  TransactionsTitleBar(
    loadedAccount = LoadedAccount.Loading,
    onAction = {},
  )
}

@Preview
@Composable
private fun TitleBarSpecific() = PreviewColumn {
  TransactionsTitleBar(
    loadedAccount = LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT),
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun EmptyTable() = PreviewScreen {
  TransactionsScaffold(
    loadedAccount = LoadedAccount.AllAccounts,
    format = TransactionsFormat.Table,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Descending),
    provider = StateProvider.Empty,
    transactions = persistentListOf(),
    onAction = {},
  )
}
