package aktual.budget.list.ui

import aktual.budget.list.ui.ListBudgetsAction.ChangePassword
import aktual.budget.list.ui.ListBudgetsAction.ChangeServer
import aktual.budget.list.ui.ListBudgetsAction.OpenAbout
import aktual.budget.list.ui.ListBudgetsAction.OpenSettings
import aktual.core.ui.BasicIconButton
import aktual.core.ui.normalIconButton
import aktual.l10n.Strings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun TopBarActions(
  onAction: (ListBudgetsAction) -> Unit,
)

@Composable
internal fun SettingsButton(
  onAction: (ListBudgetsAction) -> Unit,
  modifier: Modifier = Modifier,
) = BasicIconButton(
  modifier = modifier,
  onClick = { onAction(OpenSettings) },
  imageVector = Icons.Filled.Settings,
  contentDescription = Strings.listBudgetsSettings,
  colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
)

@Composable
internal fun MoreButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) = BasicIconButton(
  modifier = modifier,
  onClick = onClick,
  imageVector = Icons.Filled.MoreVert,
  contentDescription = Strings.listBudgetsMenu,
  colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
)

@Composable
internal fun MoreMenu(
  showMenu: Boolean,
  onAction: (ListBudgetsAction) -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) = DropdownMenu(
  modifier = modifier,
  expanded = showMenu,
  onDismissRequest = onDismissRequest,
) {
  val serverText = Strings.listBudgetsChangeServer
  DropdownMenuItem(
    text = { Text(serverText) },
    onClick = {
      onDismissRequest()
      onAction(ChangeServer)
    },
    leadingIcon = { Icon(Icons.Filled.Cloud, contentDescription = serverText) },
  )

  val passwordText = Strings.listBudgetsChangePassword
  DropdownMenuItem(
    text = { Text(passwordText) },
    onClick = {
      onDismissRequest()
      onAction(ChangePassword)
    },
    leadingIcon = { Icon(Icons.Filled.Key, contentDescription = passwordText) },
  )

  val aboutText = Strings.listBudgetsAbout
  DropdownMenuItem(
    text = { Text(aboutText) },
    onClick = {
      onDismissRequest()
      onAction(OpenAbout)
    },
    leadingIcon = { Icon(Icons.Filled.Info, contentDescription = aboutText) },
  )
}
