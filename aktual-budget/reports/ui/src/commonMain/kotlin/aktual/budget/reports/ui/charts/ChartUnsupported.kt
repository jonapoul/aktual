package aktual.budget.reports.ui.charts

import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.UnsupportedData
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.plainTextClipEntry
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
internal fun UnsupportedChart(data: UnsupportedData, modifier: Modifier = Modifier) =
  Column(modifier = modifier.padding(4.dp)) {
    val type = data.type.string()
    Text(text = data.name ?: type, color = colors.pageText, overflow = Ellipsis, maxLines = 1)
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
            Filters -> Strings.reportsUnsupportedFilters
            InvalidMeta -> Strings.reportsUnsupportedInvalidMeta
            ReportType -> Strings.reportsUnsupportedType
            SankeyBudgeted -> Strings.reportsUnsupportedSankeyBudgeted
          },
        color = colors.pageTextSubdued,
        textAlign = Center,
      )
    }

    val stackTrace = data.stackTrace
    if (stackTrace != null) {
      var showError by remember { mutableStateOf(false) }
      TextButton(modifier = Modifier.align(CenterHorizontally), onClick = { showError = true }) {
        Text(Strings.reportsUnsupportedShowError)
      }

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
      TextButton(
        onClick = { scope.launch { clipboard.setClipEntry(plainTextClipEntry(stackTrace)) } }
      ) {
        Text(Strings.reportsUnsupportedErrorCopy)
      }
      TextButton(onClick = onDismiss) { Text(Strings.reportsUnsupportedErrorClose) }
    },
  ) {
    SelectionContainer(modifier = Modifier.heightIn(max = 400.dp).verticalScrollWithBar()) {
      Text(text = stackTrace, fontFamily = Monospace, fontSize = 12.sp)
    }
  }
}

@Preview
@Composable
private fun PreviewUnsupportedChart(
  @PreviewParameter(UnsupportedChartProvider::class) params: ColoredParams<UnsupportedData>
) = PreviewWithColoredParams(params) { UnsupportedChart(data = params.data) }

private class UnsupportedChartProvider :
  ColoredParameterProvider<UnsupportedData>(
    UnsupportedData(reason = Filters, type = CashFlow, name = "Groceries cash flow"),
    UnsupportedData(reason = ReportType, type = Calendar, name = null),
    UnsupportedData(
      reason = InvalidMeta,
      type = NetWorth,
      name = "Net worth",
      stackTrace = "SerializationException: Unexpected field",
    ),
  )
