package aktual.about.data

import androidx.compose.runtime.Immutable

enum class LogExportTarget {
  Share,
  Save,
  Copy,
}

@Immutable data class LogExport(val fileName: String, val text: String, val target: LogExportTarget)
