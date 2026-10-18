package aktual.about.ui.logs

import aktual.about.vm.logs.LogExportScope
import aktual.about.vm.logs.LogExportTarget
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import logcat.LogPriority

@Immutable internal sealed interface LogsAction

internal data object NavBack : LogsAction

internal data class OpenDay(val date: LocalDate) : LogsAction

internal data object DeleteAll : LogsAction

internal data class DeleteDay(val date: LocalDate) : LogsAction

internal data class Export(val scope: LogExportScope, val target: LogExportTarget) : LogsAction

internal data class SetQuery(val query: String) : LogsAction

internal data class TogglePriority(val priority: LogPriority) : LogsAction

internal data class AddTag(val tag: String) : LogsAction

internal data class RemoveTag(val tag: String) : LogsAction

internal data object Pause : LogsAction

internal data object Resume : LogsAction

@Immutable
internal fun interface LogsActionHandler {
  operator fun invoke(action: LogsAction)
}
