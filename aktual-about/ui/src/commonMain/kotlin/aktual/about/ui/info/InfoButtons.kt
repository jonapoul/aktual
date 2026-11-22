package aktual.about.ui.info

import aktual.core.icons.AktualIcons
import aktual.core.icons.AlertTriangle
import aktual.core.icons.CheveronRight
import aktual.core.icons.ExclamationOutline
import aktual.core.icons.Refresh
import aktual.core.icons.material.Archive
import aktual.core.icons.material.Description
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.OpenInNew
import aktual.core.icons.material.ReceiptLong
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardColumn
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RounderCardShape
import aktual.core.ui.padding
import aktual.theme.Colors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun InfoButtons(onAction: OnAction<InfoAction>, modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    InfoButtonGroup {
      InfoButtonItem(
        modifier = Modifier.testTag(Tags.CheckUpdatesButton),
        icon = AktualIcons.Refresh,
        title = Strings.infoCheckUpdates,
        onClick = { onAction(CheckUpdates) },
      )

      InfoButtonItem(
        modifier = Modifier.testTag(Tags.ReportButton),
        icon = AktualIcons.ExclamationOutline,
        title = Strings.infoReportIssues,
        trailingIcon = MaterialIcons.OpenInNew,
        onClick = { onAction(ReportIssue) },
      )
    }

    InfoButtonGroup {
      InfoButtonItem(
        icon = MaterialIcons.ReceiptLong,
        title = Strings.infoLogs,
        onClick = { onAction(AppLogs) },
      )

      InfoButtonItem(
        icon = AktualIcons.AlertTriangle,
        title = Strings.infoCrashes,
        onClick = { onAction(ViewCrashReports) },
      )

      InfoButtonItem(
        icon = MaterialIcons.Archive,
        title = Strings.storageToolbar,
        onClick = { onAction(ManageStorage) },
      )
    }

    InfoButtonGroup {
      InfoButtonItem(
        modifier = Modifier.testTag(Tags.LicensesButton),
        icon = MaterialIcons.Description,
        title = Strings.infoLicenses,
        onClick = { onAction(ViewLicenses) },
      )
    }
  }

@Composable
private fun InfoButtonGroup(content: @Composable ColumnScope.() -> Unit) =
  CardColumn(
    modifier = Modifier.fillMaxWidth(),
    shape = RounderCardShape,
    contentPadding = Dimens.Large.padding,
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
    content = content,
  )

@Composable
private fun InfoButtonItem(
  icon: ImageVector,
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  trailingIcon: ImageVector = AktualIcons.CheveronRight,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(ItemHeight)
        .clickable(role = Button, onClick = onClick)
        .padding(horizontal = ItemPadding),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.padding(ItemPadding),
      imageVector = icon,
      contentDescription = null,
      tint = colors.pillText,
    )

    Text(
      modifier = Modifier.weight(1f).padding(ItemPadding),
      text = title,
      style = typography.bodyLarge,
      color = colors.pillText,
    )

    Icon(
      modifier = Modifier.padding(ItemPadding),
      imageVector = trailingIcon,
      contentDescription = null,
      tint = colors.pillText,
    )
  }
}

private val ItemPadding = 8.dp
private val ItemHeight = 50.dp

@Composable
@Preview(name = "Narrow", widthDp = 300)
@Preview(name = "Wide", widthDp = 800)
@Preview(name = "Big font", widthDp = 300, fontScale = 2f)
private fun PreviewInfoButtons(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { InfoButtons(onAction = {}) }
