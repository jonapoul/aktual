package aktual.about.vm.logs

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
sealed interface LogExportScope {
  // Lines left after the search and filters on the open day
  data object Shown : LogExportScope

  data class Day(val date: LocalDate) : LogExportScope

  data object LastWeek : LogExportScope

  data object All : LogExportScope
}

enum class LogExportTarget {
  Share,
  Save,
  Copy,
}

@Immutable data class LogExport(val fileName: String, val text: String, val target: LogExportTarget)
