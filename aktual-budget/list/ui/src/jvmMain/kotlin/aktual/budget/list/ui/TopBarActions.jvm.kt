package aktual.budget.list.ui

import aktual.core.l10n.Strings
import aktual.core.ui.BasicIconButton
import aktual.core.ui.normalIconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal actual fun TopBarActions(
  onAction: (ListBudgetsAction) -> Unit,
) = Row {
  BasicIconButton(
    modifier = Modifier.padding(horizontal = 5.dp),
    onClick = { onAction(ListBudgetsAction.Reload) },
    imageVector = Icons.Filled.Refresh,
    contentDescription = Strings.listBudgetsRefresh,
    colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
  )

  SettingsButton(
    modifier = Modifier.padding(horizontal = 5.dp),
    onAction = onAction,
  )

  var showMenu by remember { mutableStateOf(false) }
  MoreButton(
    modifier = Modifier.padding(horizontal = 5.dp),
    onClick = { showMenu = !showMenu },
  )

  MoreMenu(
    showMenu = showMenu,
    onAction = onAction,
    onDismissRequest = { showMenu = false },
  )
}
