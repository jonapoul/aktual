package actual.budget.transactions.ui

import actual.account.model.LoginToken
import actual.budget.model.AccountSpec
import actual.budget.model.BudgetId
import actual.budget.model.SortColumn
import actual.budget.model.SortDirection
import actual.budget.model.TransactionId
import actual.budget.model.TransactionsFormat
import actual.budget.model.TransactionsSpec
import actual.budget.transactions.vm.DatedTransactions
import actual.budget.transactions.vm.LoadedAccount
import actual.budget.transactions.vm.TransactionsSorting
import actual.budget.transactions.vm.TransactionsViewModel
import actual.core.ui.LocalPrivacyEnabled
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.transparentTopAppBarColors
import actual.l10n.Strings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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

  val source = remember {
    object : StateSource {
      override fun isChecked(id: TransactionId): Flow<Boolean> = viewModel.isChecked(id)
      override fun isExpanded(date: LocalDate): Flow<Boolean> = viewModel.isExpanded(date)
    }
  }

  TransactionsScaffold(
    transactions = transactions,
    observer = remember { TransactionObserver(viewModel::observe) },
    loadedAccount = loadedAccount,
    format = format,
    sorting = sorting,
    source = source,
    onAction = { action ->
      when (action) {
        Action.NavBack -> nav.back()
        is Action.CheckItem -> viewModel.setChecked(action.id, action.isChecked)
        is Action.ExpandGroup -> viewModel.setExpanded(action.group, action.isExpanded)
        is Action.SetPrivacyMode -> viewModel.setPrivacyMode(action.isPrivacyEnabled)
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
  observer: TransactionObserver,
  loadedAccount: LoadedAccount,
  format: TransactionsFormat,
  sorting: TransactionsSorting,
  source: StateSource,
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
        observer = observer,
        format = format,
        sorting = sorting,
        source = source,
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
          contentDescription = Strings.navBack,
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
    actions = {
      if (LocalPrivacyEnabled.current) {
        IconButton(
          onClick = { onAction(Action.SetPrivacyMode(isPrivacyEnabled = false)) },
          content = { Icon(Icons.Filled.VisibilityOff, contentDescription = null) },
        )
      } else {
        IconButton(
          onClick = { onAction(Action.SetPrivacyMode(isPrivacyEnabled = true)) },
          content = { Icon(Icons.Filled.Visibility, contentDescription = null) },
        )
      }
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
    observer = previewObserver(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
    format = TransactionsFormat.Table,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Descending),
    source = StateSource.Empty,
    transactions = persistentListOf(),
    onAction = {},
  )
}
