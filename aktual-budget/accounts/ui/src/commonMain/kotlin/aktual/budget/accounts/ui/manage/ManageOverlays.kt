package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.components.EditAccountSheet
import aktual.budget.accounts.components.FieldLabel
import aktual.budget.accounts.vm.close.CloseAccountEvent
import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.accounts.vm.manage.ManagedAccount
import aktual.budget.accounts.vm.manage.Success
import aktual.budget.accounts.vm.manage.isGroupNameTaken
import aktual.budget.model.Amount.Companion.Zero
import aktual.core.icons.AktualIcons
import aktual.core.icons.Pencil1
import aktual.core.icons.Trash
import aktual.core.icons.material.Archive
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ReceiptLong
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.HostedSheet
import aktual.core.ui.OnAction
import aktual.core.ui.formattedText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp

@Composable
internal fun ManageOverlays(
  overlay: ManageOverlay,
  state: Success?,
  onDismiss: (ManageOverlay) -> Unit,
  onAction: OnAction<ManageAccountsAction>,
  onClose: (CloseAccountEvent.Closed) -> Unit,
  onDelete: (CloseAccountEvent.Deleted) -> Unit,
) {
  val dismiss = { onDismiss(overlay) }
  when (overlay) {
    is AccountMenu ->
      AccountMenuSheet(
        account = overlay.account,
        groupName = state?.groups?.firstOrNull { it.id == overlay.account.group }?.name,
        onDismiss = dismiss,
        onAction = onAction,
      )

    is EditAccount -> EditAccountSheet(id = overlay.id, onDismiss = dismiss)

    is CloseAccount ->
      CloseAccountSheet(
        id = overlay.id,
        onDismiss = dismiss,
        onDeleteInstead = { onAction(Show(DeleteAccount(overlay.id))) },
        onClose = onClose,
      )

    is DeleteAccount ->
      DeleteAccountDialog(id = overlay.id, onDismiss = dismiss, onDelete = onDelete)

    CreateGroupDialog ->
      GroupNameDialog(
        title = Strings.accountsManageNewGroup,
        initial = "",
        confirmText = Strings.accountsManageGroupCreate,
        isTaken = { name -> state?.isGroupNameTaken(name) == true },
        onDismiss = dismiss,
        onConfirm = { name -> onAction(CreateGroup(name)) },
      )

    is RenameGroupDialog ->
      GroupNameDialog(
        title = Strings.accountsManageGroupRename,
        initial = overlay.group.name,
        confirmText = Strings.accountsManageGroupSave,
        isTaken = { name -> state?.isGroupNameTaken(name, except = overlay.group.id) == true },
        onDismiss = dismiss,
        onConfirm = { name -> onAction(RenameGroup(overlay.group.id, name)) },
      )

    is DeleteGroupDialog ->
      DeleteGroupConfirmDialog(
        group = overlay.group,
        accountCount = overlay.accountCount,
        onDismiss = dismiss,
        onConfirm = { onAction(DeleteGroup(overlay.group.id)) },
      )
  }
}

@Composable
private fun AccountMenuSheet(
  account: ManagedAccount,
  groupName: String?,
  onDismiss: () -> Unit,
  onAction: OnAction<ManageAccountsAction>,
) =
  HostedSheet(onDismiss = onDismiss) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = account.name,
            style = typography.headlineSmall,
            maxLines = 1,
            overflow = Ellipsis,
          )
          val budget =
            if (account.offBudget) {
              Strings.accountsManageOffBudget
            } else {
              Strings.accountsManageOnBudget
            }
          val count = account.transactionCount.toInt()
          val details =
            listOf(
              groupName ?: Strings.accountsManageUngrouped,
              budget,
              Plurals.accountsManageTransactionCount(count, count),
            )
          Text(
            text = details.joinToString(),
            style = typography.bodyMedium,
            color = colors.pageTextSubdued,
          )
        }
        Text(
          text = account.balance.formattedText(),
          style = typography.titleMedium,
          color = if (account.balance < Zero) colors.numberNegative else colors.pageText,
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colors.tableBorder)

      MenuItem(Strings.accountsManageMenuEdit, AktualIcons.Pencil1) {
        close { onAction(Show(EditAccount(account.id))) }
      }
      MenuItem(Strings.accountsManageMenuTransactions, MaterialIcons.ReceiptLong) {
        close { onAction(ViewTransactions(account.id)) }
      }
      MenuItem(Strings.accountsManageMenuClose, MaterialIcons.Archive) {
        close { onAction(Show(CloseAccount(account.id))) }
      }
      MenuItem(Strings.accountsManageMenuDelete, AktualIcons.Trash, color = colors.errorText) {
        close { onAction(Show(DeleteAccount(account.id))) }
      }
    }
  }

@Composable
private fun MenuItem(
  label: String,
  icon: ImageVector,
  color: Color = colors.pageText,
  onClick: () -> Unit,
) =
  BottomSheetListItem(
    label = label,
    isSelected = false,
    labelColor = color,
    onClick = onClick,
    leadingContent = { Icon(imageVector = icon, contentDescription = null, tint = color) },
  )

@Composable
private fun GroupNameDialog(
  title: String,
  initial: String,
  confirmText: String,
  isTaken: (String) -> Boolean,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  val nameState = rememberTextFieldState(initial)
  val name = nameState.text.toString().trim()
  val taken = name.isNotEmpty() && isTaken(name)
  val canConfirm = name.isNotEmpty() && !taken && name != initial
  AktualAlertDialog(
    title = title,
    onDismissRequest = onDismiss,
    buttons = {
      AktualButton(text = Strings.accountsManageCancel, onClick = onDismiss)
      AktualButton(
        kind = Primary,
        text = confirmText,
        isEnabled = canConfirm,
        onClick = {
          onConfirm(name)
          onDismiss()
        },
      )
    },
  ) {
    FieldLabel(modifier = Modifier.fillMaxWidth(), text = Strings.accountsManageGroupName)
    AktualTextField(
      modifier = Modifier.fillMaxWidth(),
      state = nameState,
      singleLine = true,
      keyboardOptions = KeyboardOptions(capitalization = Sentences),
      placeholderText = Strings.accountsManageGroupNamePlaceholder,
      isError = taken,
      supportingText =
        if (taken) {
          { Text(Strings.accountsManageGroupNameDuplicate) }
        } else {
          null
        },
    )
  }
}

@Composable
private fun DeleteGroupConfirmDialog(
  group: AccountGroupItem,
  accountCount: Int,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) =
  AktualAlertDialog(
    title = Strings.accountsManageGroupDeleteTitle(group.name),
    highlight = colors.errorText,
    onDismissRequest = onDismiss,
    buttons = {
      AktualButton(text = Strings.accountsManageCancel, onClick = onDismiss)
      AktualButton(
        kind = Primary,
        text = Strings.accountsManageGroupDeleteConfirm,
        onClick = {
          onConfirm()
          onDismiss()
        },
      )
    },
  ) {
    Text(
      if (accountCount == 0) {
        Strings.accountsManageGroupDeleteNone
      } else {
        Plurals.accountsManageGroupDeleteMessage(accountCount, accountCount)
      },
    )
  }
