package aktual.budget.transactions.ui

import aktual.budget.transactions.vm.LoadedAccount
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.BlurredTopBarState
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun TransactionsTitleBar(
  blurState: BlurredTopBarState,
  listState: LazyListState,
  loadedAccount: LoadedAccount,
  onAction: ActionListener,
  theme: Theme = LocalTheme.current,
) {
  val title =
    when (loadedAccount) {
      LoadedAccount.AllAccounts -> Strings.transactionsTitleAll
      LoadedAccount.Loading -> Strings.transactionsTitleLoading
      is LoadedAccount.SpecificAccount ->
        loadedAccount.account.name ?: Strings.transactionsTitleNone
    }

  TopAppBar(
    modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
    colors = theme.transparentTopAppBarColors(),
    navigationIcon = { NavBackIconButton { onAction(Action.NavBack) } },
    title = { Text(text = title, maxLines = 1, overflow = Ellipsis) },
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
  @PreviewParameter(TransactionsTitleBarProvider::class) params: ThemedParams<LoadedAccount>
) =
  PreviewWithColorScheme(params.theme) {
    TransactionsTitleBar(
      blurState = rememberBlurredTopBarState(),
      listState = rememberLazyListState(),
      loadedAccount = params.data,
      onAction = {},
    )
  }

private class TransactionsTitleBarProvider :
  ThemedParameterProvider<LoadedAccount>(
    LoadedAccount.AllAccounts,
    LoadedAccount.Loading,
    LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT),
  )
