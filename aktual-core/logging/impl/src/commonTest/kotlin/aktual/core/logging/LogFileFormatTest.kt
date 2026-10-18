package aktual.core.logging

import aktual.core.logging.LogFileFormat.fileName
import aktual.core.logging.LogFileFormat.format
import aktual.core.logging.LogFileFormat.parse
import aktual.core.logging.LogFileFormat.parseFileName
import aktual.test.isEqualToList
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class LogFileFormatTest {
  @Test
  fun `Format and parse a line with a tag`() {
    val line = format(TIME, INFO, "Main", "App started")
    assertThat(line).isEqualTo("2026-02-07 16:15:22.572 / I / Main / App started")
    assertThat(parse(line)).isEqualToList(LogLine(TIME, INFO, "Main", "App started"))
  }

  @Test
  fun `Parse older lines without a tag`() {
    val text = "2026-02-07 16:15:22.572 / D / GET /info -> 200"
    assertThat(parse(text))
      .isEqualToList(LogLine(TIME, DEBUG, tag = null, message = "GET /info -> 200"))
  }

  @Test
  fun `Keep separators inside the message`() {
    val text = "2026-02-07 16:15:22.572 / W / Api / a / b"
    assertThat(parse(text)).isEqualToList(LogLine(TIME, WARN, "Api", "a / b"))
  }

  @Test
  fun `Join lines without a timestamp onto the previous message`() {
    val text =
      """
      2026-02-07 16:15:22.572 / E / Api / Failed
      java.lang.Exception: boom
        at Foo.bar(Foo.kt:1)
      2026-02-07 16:15:22.572 / I / Api / Next

      """
        .trimIndent()

    assertThat(parse(text))
      .isEqualToList(
        LogLine(TIME, ERROR, "Api", "Failed\njava.lang.Exception: boom\n  at Foo.bar(Foo.kt:1)"),
        LogLine(TIME, INFO, "Api", "Next"),
      )
  }

  @Test
  fun `Parse file names`() {
    val date = LocalDate(2026, 2, 7)
    assertThat(fileName(date)).isEqualTo("log-2026-02-07.log")
    assertThat(parseFileName("log-2026-02-07.log")).isEqualTo(date)
    assertThat(parseFileName("something.txt")).isNull()
  }

  private companion object {
    val TIME = LocalDateTime(2026, 2, 7, 16, 15, 22, nanosecond = 572_000_000)
  }
}
