package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.accounts.vm.manage.ManagedAccount
import aktual.budget.accounts.vm.manage.ManagedGroup
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.material.Description
import aktual.core.icons.material.DragIndicator
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.BareIconButton
import aktual.core.ui.Dimens
import aktual.core.ui.OnAction
import aktual.core.ui.formattedText
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp

@Composable
internal fun SectionHeader(title: String, total: Amount, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = Dimens.ScreenPadding)
        .padding(top = ManageAccountsDS.sectionSpacing, bottom = 6.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = title.uppercase(),
      fontSize = textSizes.small,
      fontWeight = SemiBold,
      color = colors.pageTextSubdued,
    )
    Text(
      text = total.formattedText(),
      fontSize = textSizes.small,
      fontWeight = Medium,
      color = total.color(colors.pageTextSubdued),
    )
  }
}

@Composable
internal fun GroupHeader(
  group: ManagedGroup,
  onAction: OnAction<ManageAccountsAction>,
  modifier: Modifier = Modifier,
  handleModifier: Modifier = Modifier,
) {
  val id = group.id
  val name = group.displayName()
  Column(modifier = modifier.fillMaxWidth().background(colors.tableRowHeaderBackground)) {
    HorizontalDivider(color = colors.tableBorder)
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .height(ManageAccountsDS.headerHeight)
          .padding(start = ManageAccountsDS.rowPadding),
      verticalAlignment = CenterVertically,
    ) {
      Text(
        modifier = Modifier.weight(1f),
        text = name,
        fontSize = textSizes.medium,
        fontWeight = SemiBold,
        color = colors.tableRowHeaderText,
        maxLines = 1,
        overflow = Ellipsis,
      )
      Text(
        text = group.total.formattedText(),
        fontSize = textSizes.smallPlus,
        fontWeight = Medium,
        color = group.total.color(colors.tableRowHeaderText),
      )
      if (id == null) {
        Spacer(Modifier.width(ManageAccountsDS.rowPadding))
      } else {
        GroupMenu(
          name = name,
          onRename = { onAction(Show(RenameGroupDialog(AccountGroupItem(id, group.name)))) },
          onDelete = {
            val item = AccountGroupItem(id, group.name)
            onAction(Show(DeleteGroupDialog(item, group.accounts.size)))
          },
        )
        DragHandle(modifier = handleModifier, name = name)
      }
    }
  }
}

@Composable
private fun GroupMenu(name: String, onRename: () -> Unit, onDelete: () -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    BareIconButton(
      imageVector = AktualIcons.DotsHorizontalTriple,
      contentDescription = Strings.accountsManageGroupOptions(name),
      onClick = { expanded = true },
    )
    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AktualDropdownMenuItem(
        text = Strings.accountsManageGroupRename,
        onClick = {
          expanded = false
          onRename()
        },
      )
      AktualDropdownMenuItem(
        text = { Text(Strings.accountsManageGroupDelete, color = colors.errorText) },
        onClick = {
          expanded = false
          onDelete()
        },
      )
    }
  }
}

@Composable
internal fun EmptyGroupRow(modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    HorizontalDivider(color = colors.tableBorder)
    Text(
      modifier = Modifier.fillMaxWidth().padding(ManageAccountsDS.rowPadding),
      text = Strings.accountsManageGroupEmpty,
      fontSize = textSizes.smallPlus,
      color = colors.tableTextSubdued,
    )
  }
}

@Composable
internal fun AccountRow(
  account: ManagedAccount,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  handleModifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    HorizontalDivider(color = colors.tableBorder)
    Row(
      modifier = Modifier.fillMaxWidth().height(ManageAccountsDS.rowHeight),
      verticalAlignment = CenterVertically,
    ) {
      Row(
        modifier =
          Modifier.weight(1f)
            .fillMaxHeight()
            .clickable(role = Button, onClick = onClick)
            .padding(start = ManageAccountsDS.rowPadding),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              modifier = Modifier.weight(1f, fill = false),
              text = account.name,
              fontSize = textSizes.medium,
              fontWeight = Medium,
              color = colors.tableText,
              maxLines = 1,
              overflow = Ellipsis,
            )
            if (account.hasNotes) {
              Icon(
                modifier = Modifier.size(14.dp),
                imageVector = MaterialIcons.Description,
                contentDescription = Strings.accountsManageHasNotes,
                tint = colors.tableTextSubdued,
              )
            }
          }
          TransactionCount(account.transactionCount)
        }
        Text(
          text = account.balance.formattedText(),
          fontSize = textSizes.medium,
          fontWeight = Medium,
          color = account.balance.color(colors.tableText),
        )
      }
      DragHandle(modifier = handleModifier, name = account.name)
    }
  }
}

@Composable
private fun DragHandle(name: String, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.width(ManageAccountsDS.handleWidth).fillMaxHeight(),
    contentAlignment = Center,
  ) {
    Icon(
      modifier = Modifier.size(20.dp),
      imageVector = MaterialIcons.DragIndicator,
      contentDescription = Strings.accountsManageDrag(name),
      tint = colors.tableTextSubdued,
    )
  }
}

@Composable
internal fun ClosedHeader(
  count: Int,
  isExpanded: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rotation by animateFloatAsState(if (isExpanded) 0f else COLLAPSED_ROTATION)
  val label =
    if (isExpanded) Strings.accountsManageClosedCollapse else Strings.accountsManageClosedExpand
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.tableRowHeaderBackground)
        .clickable(onClickLabel = label, role = Button, onClick = onToggle)
        .height(ManageAccountsDS.headerHeight)
        .padding(horizontal = ManageAccountsDS.rowPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Icon(
      modifier = Modifier.size(16.dp).rotate(rotation),
      imageVector = AktualIcons.CheveronDown,
      contentDescription = null,
      tint = colors.tableTextSubdued,
    )
    Text(
      modifier = Modifier.weight(1f),
      text = Strings.accountsManageClosed,
      fontSize = textSizes.medium,
      fontWeight = SemiBold,
      color = colors.tableTextSubdued,
    )
    Text(text = count.toString(), fontSize = textSizes.smallPlus, color = colors.tableTextSubdued)
  }
}

@Composable
internal fun ClosedAccountRow(
  account: ManagedAccount,
  onReopen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    HorizontalDivider(color = colors.tableBorder)
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .height(ManageAccountsDS.rowHeight)
          .padding(horizontal = ManageAccountsDS.rowPadding),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Column(
        modifier = Modifier.weight(1f).alpha(CLOSED_ALPHA),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = account.name,
          fontSize = textSizes.medium,
          fontWeight = Medium,
          color = colors.tableText,
          maxLines = 1,
          overflow = Ellipsis,
        )
        TransactionCount(account.transactionCount)
      }
      AktualButton(text = Strings.accountsManageReopen, onClick = onReopen)
    }
  }
}

@Composable
private fun TransactionCount(count: Long) {
  val value = count.toInt()
  Text(
    text = Plurals.accountsManageTransactionCount(value, value),
    fontSize = textSizes.smallPlus,
    color = colors.tableTextSubdued,
  )
}

@Composable
internal fun ManagedGroup.displayName(): String =
  if (id == null) Strings.accountsManageUngrouped else name

@Composable
@ReadOnlyComposable
private fun Amount.color(default: Color) = if (this < Zero) colors.numberNegative else default

private const val COLLAPSED_ROTATION = -90f
private const val CLOSED_ALPHA = 0.7f
