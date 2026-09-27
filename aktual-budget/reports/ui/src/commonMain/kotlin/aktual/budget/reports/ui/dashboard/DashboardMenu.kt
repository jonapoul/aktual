package aktual.budget.reports.ui.dashboard

import aktual.budget.reports.ui.Action.CreatePage
import aktual.budget.reports.ui.Action.DeletePage
import aktual.budget.reports.ui.Action.RenamePage
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.core.icons.material.Add
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

private enum class PageDialog {
  Create,
  Rename,
  Delete,
}

// Acts on the selected page. Upstream refuses to delete the last one
@Composable
internal fun DashboardMenu(
  page: DashboardPage?,
  canDelete: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  var dialog by remember { mutableStateOf<PageDialog?>(null) }

  Box(modifier = modifier) {
    IconButton(onClick = { expanded = true }) {
      Icon(
        imageVector = MaterialIcons.MoreVert,
        contentDescription = Strings.reportsDashboardPageMenu,
      )
    }

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DashboardMenuItems(
        hasPage = page != null,
        canDelete = canDelete,
        onClick = { clicked ->
          expanded = false
          dialog = clicked
        },
      )
    }
  }

  val onDismiss = { dialog = null }
  when (dialog) {
    null -> Unit

    Create ->
      NameDialog(
        title = Strings.reportsDashboardNewPage,
        placeholder = Strings.reportsDashboardPageName,
        confirmText = Strings.reportsDashboardNameCreate,
        onConfirm = { name ->
          dialog = null
          onAction(CreatePage(name))
        },
        onDismiss = onDismiss,
      )

    Rename ->
      if (page != null) {
        NameDialog(
          title = Strings.reportsDashboardRenamePage,
          placeholder = Strings.reportsDashboardPageName,
          confirmText = Strings.reportsDashboardNameSave,
          initialName = page.name,
          onConfirm = { name ->
            dialog = null
            onAction(RenamePage(page.id, name))
          },
          onDismiss = onDismiss,
        )
      }

    Delete ->
      if (page != null) {
        AktualAlertDialog(onDismissRequest = onDismiss) {
          DeletePageDialogContent(
            page = page,
            onConfirm = {
              dialog = null
              onAction(DeletePage(page.id))
            },
            onDismiss = onDismiss,
          )
        }
      }
  }
}

@Composable
private fun DashboardMenuItems(
  hasPage: Boolean,
  canDelete: Boolean,
  onClick: (PageDialog) -> Unit,
) {
  AktualDropdownMenuItem(
    text = Strings.reportsDashboardNewPage,
    leadingIcon = MaterialIcons.Add,
    onClick = { onClick(Create) },
  )

  if (hasPage) {
    AktualDropdownMenuItem(
      text = Strings.reportsDashboardRenamePage,
      leadingIcon = MaterialIcons.Edit,
      onClick = { onClick(Rename) },
    )
  }

  if (hasPage && canDelete) {
    val deleteText = Strings.reportsDashboardDeletePage
    AktualDropdownMenuItem(
      text = { Text(deleteText, color = colors.errorText) },
      leadingIcon = { Icon(MaterialIcons.Delete, deleteText, tint = colors.errorText) },
      onClick = { onClick(Delete) },
    )
  }
}

@Composable
private fun DeletePageDialogContent(
  page: DashboardPage,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialogContent(
    title = Strings.reportsDashboardDeletePageTitle(page.displayName()),
    titleColor = colors.errorText,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.reportsDashboardDeletePageCancel) }
      TextButton(onClick = onConfirm) {
        Text(Strings.reportsDashboardDeletePageConfirm, color = colors.errorText)
      }
    },
    content = { Text(Strings.reportsDashboardDeletePageMessage) },
  )

@Preview
@Composable
private fun PreviewDashboardMenuItems(
  @PreviewParameter(DashboardMenuItemsProvider::class) params: ColoredParams<Boolean>
) =
  PreviewWithColoredParams(params) {
    Column(Modifier.width(IntrinsicSize.Max).background(colors.menuBackground)) {
      DashboardMenuItems(hasPage = true, canDelete = params.data, onClick = {})
    }
  }

@Preview
@Composable
private fun PreviewDeletePageDialog(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    DeletePageDialogContent(page = PREVIEW_PAGES.first(), onConfirm = {}, onDismiss = {})
  }

private class DashboardMenuItemsProvider : ColoredParameterProvider<Boolean>(true, false)
