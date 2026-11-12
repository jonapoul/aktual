package aktual.crashes.ui

import aktual.core.logging.CrashReport
import aktual.core.model.CrashFile
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char

internal fun CrashReport.timeLabel(): String = TIME_FORMAT.format(time)

private val TIME_FORMAT = LocalDateTime.Format {
  year()
  char('-')
  monthNumber()
  char('-')
  day()
  char(' ')
  hour()
  char(':')
  minute()
  char(':')
  second()
}

internal val PREVIEW_REPORT =
  CrashReport(
    file = CrashFile("crash-20261018-143012-345.txt"),
    time = LocalDateTime(LocalDate(2026, 10, 18), LocalTime(14, 30, 12)),
    version = "1.4.0 (abc1234)",
    platform = "Android 16 (API 36), Google Pixel 9",
    thread = "main",
    stackTrace =
      """
      java.lang.IllegalStateException: Budget file is missing
      	at aktual.budget.utils.BudgetFiles.open(BudgetFiles.kt:42)
      	at aktual.budget.vm.BudgetViewModel.load(BudgetViewModel.kt:87)
      	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108)
      """
        .trimIndent(),
  )
