package aktual.budget.transactions.ui

import aktual.budget.transactions.vm.LoadedAccount
import aktual.core.icons.ArrowBack
import aktual.core.icons.MaterialIcons
import aktual.core.icons.Visibility
import aktual.core.icons.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

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
          imageVector = MaterialIcons.ArrowBack,
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
          content = { Icon(MaterialIcons.VisibilityOff, Strings.transactionsHeaderPrivacyOff) },
        )
      } else {
        IconButton(
          onClick = { onAction(Action.SetPrivacyMode(isPrivacyEnabled = true)) },
          content = { Icon(MaterialIcons.Visibility, Strings.transactionsHeaderPrivacyOn) },
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
