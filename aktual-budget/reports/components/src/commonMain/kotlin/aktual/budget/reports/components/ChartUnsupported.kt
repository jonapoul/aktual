package aktual.budget.reports.components

import aktual.budget.reports.vm.UnsupportedData
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DialogTextButton
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.plainTextClipEntry
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
internal fun UnsupportedChart(data: UnsupportedData, modifier: Modifier = Modifier) =
  Column(modifier = modifier.padding(4.dp)) {
    val type = data.type.string()
    Text(text = data.name ?: type, overflow = Ellipsis, maxLines = 1)
    if (data.name != null) {
      Text(text = type, color = colors.pageTextSubdued, overflow = Ellipsis, maxLines = 1)
    }

    Box(
      modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
      contentAlignment = Center,
    ) {
      Text(
        text =
          when (data.reason) {
            CustomBudgeted -> Strings.reportsUnsupportedCustomBudgeted
            InvalidMeta -> Strings.reportsUnsupportedInvalidMeta
            MissingReport -> Strings.reportsUnsupportedMissing
            ReportType -> Strings.reportsUnsupportedType
          },
        color = colors.pageTextSubdued,
        textAlign = Center,
      )
    }

    val stackTrace = data.stackTrace
    if (stackTrace != null) {
      var showError by remember { mutableStateOf(false) }
      AktualButton(
        modifier = Modifier.align(CenterHorizontally),
        text = Strings.reportsUnsupportedShowError,
        onClick = { showError = true },
      )

      if (showError) {
        StackTraceDialog(stackTrace = stackTrace, onDismiss = { showError = false })
      }
    }
  }

@Composable
private fun StackTraceDialog(stackTrace: String, onDismiss: () -> Unit) {
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  AktualAlertDialog(
    title = Strings.reportsUnsupportedErrorTitle,
    onDismissRequest = onDismiss,
    buttons = {
      DialogTextButton(
        text = Strings.reportsUnsupportedErrorCopy,
        onClick = { scope.launch { clipboard.setClipEntry(plainTextClipEntry(stackTrace)) } },
      )
      DialogTextButton(
        kind = Primary,
        text = Strings.reportsUnsupportedErrorClose,
        onClick = onDismiss,
      )
    },
  ) {
    SelectionContainer(modifier = Modifier.heightIn(max = 400.dp).verticalScrollWithBar()) {
      Text(text = stackTrace, fontFamily = Monospace, fontSize = textSizes.small)
    }
  }
}

@Preview
@Composable
private fun PreviewUnsupportedChart(
  @PreviewParameter(UnsupportedChartProvider::class) params: ColorParams<UnsupportedData>,
) = PreviewWithColorParams(params) { UnsupportedChart(data = params.data) }

private class UnsupportedChartProvider :
  ColorParamsProvider<UnsupportedData>(
    UnsupportedData(reason = ReportType, type = Calendar, name = null),
    UnsupportedData(
      reason = InvalidMeta,
      type = NetWorth,
      name = "Net worth",
      stackTrace = "SerializationException: Unexpected field",
    ),
  )
