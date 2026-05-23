package aktual.budget.list.ui

import aktual.core.icons.material.BarChart
import aktual.core.icons.material.Cloud
import aktual.core.icons.material.Info
import aktual.core.icons.material.Key
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Settings
import aktual.core.l10n.Strings
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.BasicIconButton
import aktual.core.ui.normalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable internal expect fun TopBarActions(onAction: ListBudgetsActionHandler)

@Composable
internal fun SettingsButton(onAction: ListBudgetsActionHandler, modifier: Modifier = Modifier) =
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
  onAction: ListBudgetsActionHandler,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualDropdownMenu(
    modifier = modifier,
    expanded = showMenu,
    onDismissRequest = onDismissRequest,
  ) {
    val serverText = Strings.listBudgetsChangeServer
    AktualDropdownMenuItem(
      text = { Text(serverText) },
      onClick = {
        onDismissRequest()
        onAction(LogOut)
      },
      leadingIcon = { Icon(MaterialIcons.Cloud, contentDescription = serverText) },
    )

    val passwordText = Strings.listBudgetsChangePassword
    AktualDropdownMenuItem(
      text = { Text(passwordText) },
      onClick = {
        onDismissRequest()
        onAction(ChangePassword)
      },
      leadingIcon = { Icon(MaterialIcons.Key, contentDescription = passwordText) },
    )

    val metricsText = Strings.metricsToolbar
    AktualDropdownMenuItem(
      text = { Text(metricsText) },
      onClick = {
        onDismissRequest()
        onAction(OpenServerMetrics)
      },
      leadingIcon = { Icon(MaterialIcons.BarChart, contentDescription = metricsText) },
    )

    val aboutText = Strings.listBudgetsAbout
    AktualDropdownMenuItem(
      text = { Text(aboutText) },
      onClick = {
        onDismissRequest()
        onAction(OpenAbout)
      },
      leadingIcon = { Icon(MaterialIcons.Info, contentDescription = aboutText) },
    )
  }
}
