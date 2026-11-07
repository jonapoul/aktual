package aktual.crashes.data

import aktual.core.logging.CrashReport
import aktual.core.model.CrashFile
import aktual.test.TestBuildConfig
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.startsWith
import io.ktor.http.Url
import kotlin.test.Test
import kotlinx.datetime.LocalDateTime

class CrashIssueUrlBuilderTest {
  private val builder = CrashIssueUrlBuilder(TestBuildConfig)

  @Test
  fun `Prefill the bug report form`() {
    val url = Url(builder.build(REPORT))

    assertThat(url.toString()).startsWith("https://github.com/jonapoul/aktual/issues/new?")
    with(url.parameters) {
      assertThat(get("template")).isEqualTo("bug_report.yml")
      assertThat(get("title")).isEqualTo("java.lang.IllegalStateException: Boom")
      assertThat(get("version")).isEqualTo("1.2.3 (abc)")
      assertThat(get("platform")).isEqualTo("Android")
      assertThat(get("crash-report"))
        .isEqualTo(
          """
          |Time: 2026-02-07T16:15:22.572
          |Platform: Android 16 (API 36), Pixel
          |Thread: main
          |
          |${REPORT.stackTrace}
          """
            .trimMargin(),
        )
    }
  }

  @Test
  fun `Clip long stack traces`() {
    val trace = "java.lang.Error: Deep\n" + "\tat Foo.bar(Foo.kt:1)\n".repeat(1_000)
    val url = builder.build(REPORT.copy(stackTrace = trace, platform = "Linux"))

    assertThat(url.length).isLessThan(8_000)
    assertThat(Url(url).parameters["platform"]).isEqualTo("Desktop")
  }

  private companion object {
    val REPORT =
      CrashReport(
        file = CrashFile("crash-20260207-161522-572.txt"),
        time = LocalDateTime(2026, 2, 7, 16, 15, 22, 572_000_000),
        version = "1.2.3 (abc)",
        platform = "Android 16 (API 36), Pixel",
        thread = "main",
        stackTrace = "java.lang.IllegalStateException: Boom\n\tat Foo.bar(Foo.kt:1)",
      )
  }
}
