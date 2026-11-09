package aktual.about.ui.info

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.CardColumn
import aktual.core.ui.ColorParameters
import aktual.core.ui.NormalTextButton
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RounderCardShape
import aktual.core.ui.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
@Suppress("MagicNumber")
internal fun InfoButtons(onAction: OnAction<InfoAction>, modifier: Modifier = Modifier) =
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val density = LocalDensity.current

    @Suppress("RestrictedApi") val fontScale = density.fontScale

    val narrowBreakpoint = (400 * fontScale).dp
    val wideBreakpoint = (700 * fontScale).dp

    val availableWidth = maxWidth
    when {
      availableWidth < narrowBreakpoint -> InfoButtonsColumn(onAction)
      availableWidth < wideBreakpoint -> InfoButtonsGrid(onAction)
      else -> InfoButtonsRow(onAction)
    }
  }

@Composable
private fun CheckUpdatesButton(onAction: OnAction<InfoAction>, modifier: Modifier = Modifier) =
  NormalTextButton(
    modifier = modifier.testTag(Tags.CheckUpdatesButton),
    text = Strings.infoCheckUpdates,
    onClick = { onAction(CheckUpdates) },
  )

@Composable
private fun ReportButton(onAction: OnAction<InfoAction>, modifier: Modifier = Modifier) =
  NormalTextButton(
    modifier = modifier.testTag(Tags.ReportButton),
    text = Strings.infoReportIssues,
    onClick = { onAction(ReportIssue) },
  )

@Composable
private fun LicensesButton(onAction: OnAction<InfoAction>, modifier: Modifier = Modifier) =
  NormalTextButton(
    modifier = modifier.testTag(Tags.LicensesButton),
    text = Strings.infoLicenses,
    onClick = { onAction(ViewLicenses) },
  )

@Composable
private fun ManageStorageButton(onAction: OnAction<InfoAction>, modifier: Modifier = Modifier) =
  NormalTextButton(
    modifier = modifier,
    text = Strings.storageToolbar,
    onClick = { onAction(ManageStorage) },
  )

@Composable
private fun LogsButton(onAction: OnAction<InfoAction>, modifier: Modifier = Modifier) =
  NormalTextButton(
    modifier = modifier,
    text = Strings.infoLogs,
    onClick = { onAction(AppLogs) },
  )

@Composable
private fun CrashReportsButton(onAction: OnAction<InfoAction>, modifier: Modifier = Modifier) =
  NormalTextButton(
    modifier = modifier,
    text = Strings.infoCrashes,
    onClick = { onAction(ViewCrashReports) },
  )

private val ButtonCardPadding = 10.dp.padding

@Composable
private fun InfoButtonsColumn(onAction: OnAction<InfoAction>) =
  CardColumn(
    modifier = Modifier.fillMaxWidth(),
    shape = RounderCardShape,
    contentPadding = ButtonCardPadding,
    horizontalAlignment = CenterHorizontally,
  ) {
    val buttonModifier = Modifier.fillMaxWidth()
    CheckUpdatesButton(onAction, buttonModifier)
    ReportButton(onAction, buttonModifier)
    LicensesButton(onAction, buttonModifier)
    ManageStorageButton(onAction, buttonModifier)
    LogsButton(onAction, buttonModifier)
    CrashReportsButton(onAction, buttonModifier)
  }

@Composable
private fun InfoButtonsGrid(onAction: OnAction<InfoAction>) =
  CardColumn(
    modifier = Modifier.fillMaxWidth(),
    shape = RounderCardShape,
    contentPadding = ButtonCardPadding,
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(0.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_SPACING),
    ) {
      CheckUpdatesButton(onAction, Modifier.weight(1f))
      ReportButton(onAction, Modifier.weight(1f))
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_SPACING),
    ) {
      LicensesButton(onAction, Modifier.weight(1f))
      ManageStorageButton(onAction, Modifier.weight(1f))
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_SPACING),
    ) {
      LogsButton(onAction, Modifier.weight(1f))
      CrashReportsButton(onAction, Modifier.weight(1f))
    }
  }

@Composable
private fun InfoButtonsRow(onAction: OnAction<InfoAction>) =
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_SPACING),
  ) {
    CheckUpdatesButton(onAction, Modifier.weight(1f))
    ReportButton(onAction, Modifier.weight(1f))
    LicensesButton(onAction, Modifier.weight(1f))
    ManageStorageButton(onAction, Modifier.weight(1f))
    LogsButton(onAction, Modifier.weight(1f))
    CrashReportsButton(onAction, Modifier.weight(1f))
  }

private val HORIZONTAL_SPACING = 8.dp

@Composable
@Preview(name = "Narrow", widthDp = 300)
@Preview(name = "Medium", widthDp = 500)
@Preview(name = "Medium - small font", widthDp = 500, fontScale = 0.65f)
@Preview(name = "Wide", widthDp = 800)
@Preview(name = "Wide - big font", widthDp = 800, fontScale = 2f)
private fun PreviewInfoButtons(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { InfoButtons(onAction = {}) }
