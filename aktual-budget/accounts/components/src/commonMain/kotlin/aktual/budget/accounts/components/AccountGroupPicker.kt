package aktual.budget.accounts.components

import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.model.AccountGroupId
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// Ungrouped, which is null, then every group in order
@Composable
fun AccountGroupPicker(
  value: AccountGroupId?,
  groups: ImmutableList<AccountGroupItem>,
  onValueChange: (AccountGroupId?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val options =
    remember(groups) { (listOf<AccountGroupId?>(null) + groups.map { it.id }).toImmutableList() }
  val ungrouped = Strings.accountsManageUngrouped
  AktualExposedDropDownMenu(
    modifier = modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    options = options,
    string = { id -> groups.firstOrNull { it.id == id }?.name ?: ungrouped },
  )
}
