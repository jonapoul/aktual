package aktual.core.logging

import aktual.core.model.CrashFile
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.datetime.LocalDateTime

class CrashReportFormatTest {
  @Test
  fun `Format and parse round trip`() {
    val text = CrashReportFormat.format(REPORT)

    assertThat(text)
      .isEqualTo(
        """
        |time: 2026-02-07 16:15:22.572
        |version: 1.2.3 (abc)
        |platform: Linux
        |thread: main
        |
        |java.lang.IllegalStateException: Boom
        |    at Foo.bar(Foo.kt:1)
        |"""
          .trimMargin(),
      )
    assertThat(CrashReportFormat.parse(REPORT.file, text)).isEqualTo(REPORT)
  }

  @Test
  fun `File names`() {
    assertThat(CrashReportFormat.file(TIME)).isEqualTo(CrashFile("crash-20260207-161522-572.txt"))
    assertThat(CrashReportFormat.isCrashFile("crash-20260207-161522-572.txt")).isTrue()
    assertThat(CrashReportFormat.isCrashFile("log-2026-02-07.log")).isFalse()
    assertThat(CrashReportFormat.isCrashFile("../crash-20260207-161522-572.txt")).isFalse()
  }

  @Test
  fun `Parse falls back to the file name time`() {
    val report = CrashReportFormat.parse(REPORT.file, "garbage")

    assertThat(report?.time).isEqualTo(TIME)
    assertThat(CrashReportFormat.parse(CrashFile("other.txt"), "garbage")).isNull()
  }

  private companion object {
    const val TRACE =
      """
      |java.lang.IllegalStateException: Boom
      |    at Foo.bar(Foo.kt:1)
      """
        .trimMargin()

    val TIME = LocalDateTime(2026, 2, 7, 16, 15, 22, 572_000_000)
    val REPORT =
      CrashReport(
        file = CrashFile("crash-20260207-161522-572.txt"),
        time = TIME,
        version = "1.2.3 (abc)",
        platform = "Linux",
        thread = "main",
        stackTrace = TRACE,
      )
  }
}
