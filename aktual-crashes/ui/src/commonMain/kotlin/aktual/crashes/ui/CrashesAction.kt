package aktual.crashes.ui

import aktual.about.data.LogExportTarget
import aktual.core.model.CrashFile
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface CrashesAction

internal data object NavBack : CrashesAction

internal data class Open(val file: CrashFile) : CrashesAction

internal data class Delete(val file: CrashFile) : CrashesAction

internal data object DeleteAll : CrashesAction

internal data class Export(val includeLogs: Boolean, val target: LogExportTarget) : CrashesAction

internal data object ReportIssue : CrashesAction

@Immutable
internal fun interface CrashesActionHandler {
  operator fun invoke(action: CrashesAction)
}
