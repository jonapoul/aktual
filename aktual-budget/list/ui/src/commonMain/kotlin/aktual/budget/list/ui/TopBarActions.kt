package aktual.budget.list.ui

import aktual.budget.list.ui.ListBudgetsAction.ChangePassword
import aktual.budget.list.ui.ListBudgetsAction.LogOut
import aktual.budget.list.ui.ListBudgetsAction.OpenAbout
import aktual.budget.list.ui.ListBudgetsAction.OpenServerMetrics
import aktual.budget.list.ui.ListBudgetsAction.OpenSettings
import aktual.core.icons.material.BarChart
import aktual.core.icons.material.Cloud
import aktual.core.icons.material.Info
import aktual.core.icons.material.Key
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Settings
import aktual.core.l10n.Strings
import aktual.core.ui.BasicIconButton
import aktual.core.ui.ThemedDropdownMenu
import aktual.core.ui.ThemedDropdownMenuItem
import aktual.core.ui.normalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable internal expect fun TopBarActions(onAction: (ListBudgetsAction) -> Unit)

@Composable
internal fun SettingsButton(onAction: (ListBudgetsAction) -> Unit, modifier: Modifier = Modifier) =
  BasicIconButton(
    modifier = modifier,
    onClick = { onAction(OpenSettings) },
    imageVector = MaterialIcons.Settings,
    contentDescription = Strings.listBudgetsSettings,
    colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
  )

@Composable
internal fun MoreButton(onClick: () -> Unit, modifier: Modifier = Modifier) =
  BasicIconButton(
    modifier = modifier,
    onClick = onClick,
    imageVector = MaterialIcons.MoreVert,
    contentDescription = Strings.listBudgetsMenu,
    colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
  )

@Composable
internal fun MoreMenu(
  showMenu: Boolean,
  onAction: (ListBudgetsAction) -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ThemedDropdownMenu(
    modifier = modifier,
    expanded = showMenu,
    onDismissRequest = onDismissRequest,
  ) {
    val serverText = Strings.listBudgetsChangeServer
    ThemedDropdownMenuItem(
      text = { Text(serverText) },
      onClick = {
        onDismissRequest()
        onAction(LogOut)
      },
      leadingIcon = { Icon(MaterialIcons.Cloud, contentDescription = serverText) },
    )

    val passwordText = Strings.listBudgetsChangePassword
    ThemedDropdownMenuItem(
      text = { Text(passwordText) },
      onClick = {
        onDismissRequest()
        onAction(ChangePassword)
      },
      leadingIcon = { Icon(MaterialIcons.Key, contentDescription = passwordText) },
    )

    val metricsText = Strings.metricsToolbar
    ThemedDropdownMenuItem(
      text = { Text(metricsText) },
      onClick = {
        onDismissRequest()
        onAction(OpenServerMetrics)
      },
      leadingIcon = { Icon(MaterialIcons.BarChart, contentDescription = metricsText) },
    )

    val aboutText = Strings.listBudgetsAbout
    ThemedDropdownMenuItem(
      text = { Text(aboutText) },
      onClick = {
        onDismissRequest()
        onAction(OpenAbout)
      },
      leadingIcon = { Icon(MaterialIcons.Info, contentDescription = aboutText) },
    )
  }
}
