package aktual.about.ui.info

import aktual.core.l10n.Strings
import aktual.core.model.ColorSchemeType
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColorScheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
@Suppress("MagicNumber")
internal fun InfoButtons(
    onAction: (InfoAction) -> Unit,
    modifier: Modifier = Modifier,
) =
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
      val density = LocalDensity.current
      val fontScale = density.fontScale

      // Adjust breakpoints based on font scale to accommodate larger text
      // Base values: 400dp for narrow->grid, 700dp for grid->row
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
private fun SourceCodeButton(
    onAction: (InfoAction) -> Unit,
    modifier: Modifier = Modifier,
) =
    NormalTextButton(
        modifier = modifier.testTag(Tags.SourceCodeButton),
        text = Strings.infoRepo,
        onClick = { onAction(InfoAction.OpenSourceCode) },
    )

@Composable
private fun CheckUpdatesButton(
    onAction: (InfoAction) -> Unit,
    modifier: Modifier = Modifier,
) =
    NormalTextButton(
        modifier = modifier.testTag(Tags.CheckUpdatesButton),
        text = Strings.infoCheckUpdates,
        onClick = { onAction(InfoAction.CheckUpdates) },
    )

@Composable
private fun ReportButton(
    onAction: (InfoAction) -> Unit,
    modifier: Modifier = Modifier,
) =
    NormalTextButton(
        modifier = modifier.testTag(Tags.ReportButton),
        text = Strings.infoReportIssues,
        onClick = { onAction(InfoAction.ReportIssue) },
    )

@Composable
private fun LicensesButton(
    onAction: (InfoAction) -> Unit,
    modifier: Modifier = Modifier,
) =
    NormalTextButton(
        modifier = modifier.testTag(Tags.LicensesButton),
        text = Strings.infoLicenses,
        onClick = { onAction(InfoAction.ViewLicenses) },
    )

@Composable
private fun InfoButtonsColumn(onAction: (InfoAction) -> Unit) =
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val buttonModifier = Modifier.fillMaxWidth()
      SourceCodeButton(onAction, buttonModifier)
      CheckUpdatesButton(onAction, buttonModifier)
      ReportButton(onAction, buttonModifier)
      LicensesButton(onAction, buttonModifier)
    }

@Composable
private fun InfoButtonsGrid(onAction: (InfoAction) -> Unit) =
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_SPACING),
      ) {
        SourceCodeButton(onAction, Modifier.weight(1f))
        CheckUpdatesButton(onAction, Modifier.weight(1f))
      }
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_SPACING),
      ) {
        ReportButton(onAction, Modifier.weight(1f))
        LicensesButton(onAction, Modifier.weight(1f))
      }
    }

@Composable
private fun InfoButtonsRow(onAction: (InfoAction) -> Unit) =
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_SPACING),
    ) {
      SourceCodeButton(onAction, Modifier.weight(1f))
      CheckUpdatesButton(onAction, Modifier.weight(1f))
      ReportButton(onAction, Modifier.weight(1f))
      LicensesButton(onAction, Modifier.weight(1f))
    }

private val HORIZONTAL_SPACING = 8.dp

@Composable
@Preview(name = "Narrow", widthDp = 300)
@Preview(name = "Medium", widthDp = 500)
@Preview(name = "Medium - small font", widthDp = 500, fontScale = 0.65f)
@Preview(name = "Wide", widthDp = 800)
@Preview(name = "Wide - big font", widthDp = 800, fontScale = 2f)
private fun PreviewInfoButtons(
    @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) { InfoButtons(onAction = {}) }
