package actual.budget.transactions.ui

import actual.account.model.LoginToken
import actual.budget.model.AccountView
import actual.budget.model.BudgetId
import actual.budget.model.TransactionId
import actual.budget.model.TransactionsSpec
import actual.budget.transactions.vm.DatedTransactions
import actual.budget.transactions.vm.SortBy
import actual.budget.transactions.vm.TransactionsFormat
import actual.budget.transactions.vm.TransactionsViewModel
import actual.core.res.CoreStrings
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
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
  config: TransactionsSpec = TransactionsSpec(AccountView.AllAccounts),
  viewModel: TransactionsViewModel = hiltViewModel(token, budgetId, config),
) {
  val transactions by viewModel.transactions.collectAsStateWithLifecycle()
  val title by viewModel.title.collectAsStateWithLifecycle()
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
    title = title,
    format = format,
    sorting = sorting,
    provider = provider,
    onAction = { action ->
      when (action) {
        TransactionsAction.NavBack -> nav.back()
        is TransactionsAction.CheckItem -> viewModel.setChecked(action.id, action.isChecked)
        is TransactionsAction.ExpandGroup -> viewModel.setExpanded(action.group, action.isExpanded)
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
  title: String,
  format: TransactionsFormat,
  sorting: SortBy,
  provider: StateProvider,
  onAction: (TransactionsAction) -> Unit,
) {
  val checkbox = remember {
    object : TransactionCheckbox {
      override fun isChecked(id: TransactionId): Flow<Boolean> = provider.isChecked(id)
      override fun onCheckedChange(id: TransactionId, isChecked: Boolean) =
        onAction(TransactionsAction.CheckItem(id, isChecked))
    }
  }

  val header = remember {
    object : TransactionHeader {
      override fun isExpanded(date: LocalDate): Flow<Boolean> = provider.isExpanded(date)
      override fun onExpandedChange(date: LocalDate, isExpanded: Boolean) =
        onAction(TransactionsAction.ExpandGroup(date, isExpanded))
    }
  }

  val theme = LocalTheme.current
  Scaffold(
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = {
          IconButton(onClick = { onAction(TransactionsAction.NavBack) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = CoreStrings.navBack,
            )
          }
        },
        title = { Text(text = title, maxLines = 1, overflow = Ellipsis) },
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()

      Transactions(
        modifier = Modifier.padding(innerPadding),
        transactions = transactions,
        format = format,
        sorting = sorting,
        checkbox = checkbox,
        header = header,
        theme = theme,
      )
    }
  }
}

@ScreenPreview
@Composable
private fun EmptyTable() = PreviewScreen {
  TransactionsScaffold(
    title = "All Accounts",
    format = TransactionsFormat.Table,
    sorting = SortBy.Date(ascending = false),
    provider = PreviewStateProvider,
    transactions = persistentListOf(),
    onAction = {},
  )
}
