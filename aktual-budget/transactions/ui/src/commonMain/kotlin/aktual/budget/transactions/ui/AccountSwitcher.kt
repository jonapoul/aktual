package aktual.budget.transactions.ui

import aktual.budget.model.AccountId
import aktual.budget.transactions.vm.AccountChoice
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.CheveronDown
import aktual.core.l10n.Strings
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun AccountSwitcher(
  title: String,
  selected: AccountId,
  choices: ImmutableList<AccountChoice>,
  onAction: OnAction<Action>,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    Row(
      modifier =
        Modifier.background(colors.pillBackgroundLight, CardShape)
          .clickable { expanded = true }
          .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = CenterVertically,
    ) {
      Text(
        modifier = Modifier.weight(1f, fill = false),
        text = title,
        maxLines = 1,
        overflow = Ellipsis,
      )
      Icon(
        imageVector = AktualIcons.CheveronDown,
        contentDescription = Strings.transactionsChooseAccount,
      )
    }

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      val none = Strings.transactionsTitleNone
      choices.fastForEach { choice ->
        AktualDropdownMenuItem(
          text = choice.name ?: none,
          trailingIcon = if (choice.id == selected) AktualIcons.Check else null,
          onClick = {
            expanded = false
            if (choice.id != selected) onAction(Action.SwitchAccount(choice.id))
          },
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewAccountSwitcher(
  @PreviewParameter(AccountSwitcherProvider::class) params: ColorParams<String>,
) =
  PreviewWithColorParams(params) {
    AccountSwitcher(
      title = this,
      selected = PREVIEW_ACCOUNT.id,
      choices = persistentListOf(AccountChoice(PREVIEW_ACCOUNT.id, this)),
      onAction = {},
    )
  }

private class AccountSwitcherProvider :
  ColorParamsProvider<String>("Checking", "A very long account name that won't fit in the bar")
