package aktual.crashes.dialog

import aktual.core.icons.AktualIcons
import aktual.core.icons.AlertTriangle
import aktual.core.l10n.Strings
import aktual.core.logging.CrashReport
import aktual.core.model.CrashFile
import aktual.core.nav.CrashReportNavigator
import aktual.core.nav.NavStack
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import aktual.crashes.vm.CrashPromptViewModel
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDateTime

@Composable
fun CrashPrompt(
  navStack: NavStack<NavKey>,
  viewModel: CrashPromptViewModel = metroViewModel(),
) {
  val unseen by viewModel.unseen.collectAsStateWithLifecycle()
  val report = unseen ?: return
  CrashPromptDialog(
    report = report,
    onView = {
      viewModel.markSeen()
      CrashReportNavigator(navStack)(report.file)
    },
    onDismiss = viewModel::markSeen,
  )
}

@Composable
private fun CrashPromptDialog(report: CrashReport, onView: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialog(
    title = Strings.crashesPromptTitle,
    onDismissRequest = onDismiss,
    icon = AktualIcons.AlertTriangle,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.crashesPromptDismiss) }
      TextButton(onClick = onView) {
        Text(Strings.crashesPromptView, color = colors.pageTextPositive)
      }
    },
  ) {
    Text(text = Strings.crashesPromptMessage)
    Text(text = report.summary, maxLines = 2, overflow = Ellipsis)
  }

@Preview
@Composable
private fun PreviewCrashPrompt(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { CrashPromptDialog(PREVIEW_REPORT, onView = {}, onDismiss = {}) }

private val PREVIEW_REPORT =
  CrashReport(
    file = CrashFile("crash-20261018-143012-345.txt"),
    time = LocalDateTime(2026, 10, 18, 14, 30, 12),
    version = "1.4.0 (abc1234)",
    platform = "Android 16 (API 36), Google Pixel 9",
    thread = "main",
    stackTrace = "java.lang.IllegalStateException: Budget file is missing",
  )
