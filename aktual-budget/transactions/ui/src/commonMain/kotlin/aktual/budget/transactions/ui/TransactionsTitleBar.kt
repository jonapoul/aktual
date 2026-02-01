package aktual.budget.transactions.ui

import aktual.budget.model.SyncState
import aktual.budget.transactions.vm.LoadedAccount
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun TransactionsTitleBar(
  loadedAccount: LoadedAccount,
  syncState: SyncState,
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
      IconButton(
        enabled = syncState !is SyncState.InProgress && loadedAccount !is LoadedAccount.Loading,
        onClick = { onAction(Action.Sync) },
        content = {
          val rotation by syncTransition(syncState)
          Icon(
            imageVector = Icons.Filled.Sync,
            contentDescription = Strings.transactionsSync,
            modifier = Modifier.rotate(rotation),
          )
        },
      )

      if (LocalPrivacyEnabled.current) {
        IconButton(
          onClick = { onAction(Action.SetPrivacyMode(isPrivacyEnabled = false)) },
          content = { Icon(Icons.Filled.VisibilityOff, Strings.transactionsHeaderPrivacyOff) },
        )
      } else {
        IconButton(
          onClick = { onAction(Action.SetPrivacyMode(isPrivacyEnabled = true)) },
          content = { Icon(Icons.Filled.Visibility, Strings.transactionsHeaderPrivacyOn) },
        )
      }
    },
  )
}

// anti-clockwise
@Composable
private fun syncTransition(state: SyncState): State<Float> {
  val isSync = state == SyncState.Queued || state == SyncState.InProgress
  val infiniteTransition = rememberInfiniteTransition(label = "sync rotation")
  return infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = if (isSync) -360f else 0f,
    label = "sync rotation",
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
  )
}

@Preview
@Composable
private fun PreviewTransactionsTitleBar(
  @PreviewParameter(TransactionsTitleBarProvider::class) params: ThemedParams<TTBParams>,
) = PreviewWithColorScheme(params.type) {
  TransactionsTitleBar(
    loadedAccount = params.data.loadedAccount,
    syncState = params.data.syncState,
    onAction = {},
  )
}

private data class TTBParams(
  val loadedAccount: LoadedAccount,
  val syncState: SyncState = SyncState.Inactive,
)

private class TransactionsTitleBarProvider : ThemedParameterProvider<TTBParams>(
  TTBParams(LoadedAccount.AllAccounts),
  TTBParams(LoadedAccount.Loading),
  TTBParams(LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT)),
  TTBParams(LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT), syncState = SyncState.InProgress),
)
