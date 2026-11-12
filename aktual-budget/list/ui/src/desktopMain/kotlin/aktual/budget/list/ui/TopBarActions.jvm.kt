package aktual.budget.list.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Refresh
import aktual.core.l10n.Strings
import aktual.core.ui.AktualIconButton
import aktual.core.ui.OnAction
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal actual fun TopBarActions(onAction: OnAction<ListBudgetsAction>) = Row {
  AktualIconButton(
    kind = Normal,
    modifier = Modifier.padding(horizontal = 5.dp),
    onClick = { onAction(Reload) },
    imageVector = AktualIcons.Refresh,
    contentDescription = Strings.listBudgetsRefresh,
  )

  SettingsButton(modifier = Modifier.padding(horizontal = 5.dp), onAction = onAction)

  var showMenu by remember { mutableStateOf(false) }
  MoreButton(modifier = Modifier.padding(horizontal = 5.dp), onClick = { showMenu = !showMenu })

  MoreMenu(showMenu = showMenu, onAction = onAction, onDismissRequest = { showMenu = false })
}
