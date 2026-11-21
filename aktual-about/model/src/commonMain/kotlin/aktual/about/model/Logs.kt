package aktual.about.model

import aktual.core.model.Bytes
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

enum class LogExportTarget {
  Share,
  Save,
  Copy,
}

@Immutable data class LogExport(val fileName: String, val text: String, val target: LogExportTarget)

@Immutable data class LogFile(val date: LocalDate, val size: Bytes, val errorCount: Int)
