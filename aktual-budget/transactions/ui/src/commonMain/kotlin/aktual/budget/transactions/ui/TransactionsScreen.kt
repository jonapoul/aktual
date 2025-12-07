/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.ui

import aktual.budget.model.AccountSpec
import aktual.budget.model.BudgetId
import aktual.budget.model.SortColumn
import aktual.budget.model.SortDirection
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsFormat
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.DatedTransactions
import aktual.budget.transactions.vm.LoadedAccount
import aktual.budget.transactions.vm.TransactionsSorting
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.model.ColorSchemeType
import aktual.core.model.LoginToken
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Composable
fun TransactionsScreen(
  nav: TransactionsNavigator,
  budgetId: BudgetId,
  token: LoginToken,
  spec: TransactionsSpec = TransactionsSpec(AccountSpec.AllAccounts),
  viewModel: TransactionsViewModel = metroViewModel(token, budgetId, spec),
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
private fun metroViewModel(
  token: LoginToken,
  budgetId: BudgetId,
  spec: TransactionsSpec,
) = assistedMetroViewModel<TransactionsViewModel, TransactionsViewModel.Factory> {
  create(token, budgetId, spec)
}

@Composable
internal fun TransactionsScaffold(
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
internal fun TransactionsTitleBar(
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
private fun PreviewTransactionsTitleBar(
  @PreviewParameter(TransactionsTitleBarProvider::class) params: ThemedParams<LoadedAccount>,
) = PreviewWithColorScheme(params.type) {
  TransactionsTitleBar(
    loadedAccount = params.data,
    onAction = {},
  )
}

private class TransactionsTitleBarProvider : ThemedParameterProvider<LoadedAccount>(
  LoadedAccount.AllAccounts,
  LoadedAccount.Loading,
  LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT),
)

@Composable
@PortraitPreview
private fun PreviewTransactionsScaffold(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
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
