package aktual.about.vm.logs

import aktual.about.data.LogFile
import aktual.about.data.LogsRepository
import aktual.core.logging.LogFileFormat
import aktual.core.logging.LogLine
import dev.zacsweers.metro.Inject
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

@Inject
internal class LogExporter(private val repository: LogsRepository) {
  suspend fun export(
    scope: LogExportScope,
    target: LogExportTarget,
    shown: List<LogLine> = emptyList(),
    shownDate: LocalDate? = null,
  ): LogExport {
    val today = repository.today()
    val text =
      when (scope) {
        Shown -> shown.joinToString(separator = "") { LogFileFormat.format(it) + "\n" }
        is Day -> repository.read(listOf(scope.date))
        LastWeek -> repository.read(datesSince(today.minus(DatePeriod(days = LAST_WEEK_DAYS - 1))))
        All -> repository.read(datesSince(cutoff = null))
      }

    val suffix =
      when (scope) {
        Shown -> (shownDate ?: today).toString()
        is Day -> scope.date.toString()
        LastWeek -> "last-week"
        All -> "all"
      }

    return LogExport(fileName = "aktual-logs-$suffix.txt", text = text, target = target)
  }

  private suspend fun datesSince(cutoff: LocalDate?): List<LocalDate> =
    repository.files().map(LogFile::date).filter { cutoff == null || it >= cutoff }

  private companion object {
    const val LAST_WEEK_DAYS = 7
  }
}
