package aktual.budget.accounts.ui

import aktual.core.icons.material.AccountBalance
import aktual.core.icons.material.AccountBalanceWallet
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

/**
 * Adds an account from [showSheet], where a local account or bank sync can be chosen as upstream's
 * CreateAccountModal. Without a server there's no bank sync, so it goes straight to [createLocal].
 */
fun addAccount(canBankSync: Boolean, showSheet: () -> Unit, createLocal: () -> Unit) =
  if (canBankSync) showSheet() else createLocal()

@Composable
fun AddAccountSheet(onCreateLocal: () -> Unit, onBankSync: () -> Unit, onDismiss: () -> Unit) {
  AktualModalBottomSheet(onDismissRequest = onDismiss) {
    AddAccountSheetContent(
      onCreateLocal = {
        onDismiss()
        onCreateLocal()
      },
      onBankSync = {
        onDismiss()
        onBankSync()
      },
    )
  }
}

@Composable
private fun AddAccountSheetContent(
  onCreateLocal: () -> Unit,
  onBankSync: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth().padding(bottom = 24.dp)) {
    Text(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).semantics { heading() },
      text = Strings.accountsAddTitle,
      style = typography.headlineSmall,
    )
    AddAccountOption(
      icon = MaterialIcons.AccountBalanceWallet,
      title = Strings.accountsAddLocal,
      hint = Strings.accountsAddLocalHint,
      onClick = onCreateLocal,
    )
    AddAccountOption(
      icon = MaterialIcons.AccountBalance,
      title = Strings.accountsAddBankSync,
      hint = Strings.accountsAddBankSyncHint,
      onClick = onBankSync,
    )
  }
}

@Composable
private fun AddAccountOption(
  icon: ImageVector,
  title: String,
  hint: String,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable(onClick = onClick),
    leadingContent = { BottomSheetIcon(icon) },
    headlineContent = { Text(title) },
    supportingContent = { Text(hint) },
    colors = colors.option(),
  )
}

@Composable
private fun Colors.option(): ListItemColors =
  ListItemDefaults.colors(
    containerColor = Transparent,
    headlineColor = pageText,
    supportingColor = pageTextSubdued,
    leadingIconColor = pageText,
  )

@Preview
@Composable
private fun PreviewAddAccountSheet(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { AddAccountSheetContent(onCreateLocal = {}, onBankSync = {}) }
