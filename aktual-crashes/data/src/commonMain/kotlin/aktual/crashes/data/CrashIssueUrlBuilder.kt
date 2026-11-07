package aktual.crashes.data

import aktual.core.logging.CrashReport
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.Inject
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.parameters

// Prefills the repo's bug report form. GitHub can't take file attachments through the URL, so the
// stack trace goes in the crash report field and the user attaches the exported file themselves
@Inject
class CrashIssueUrlBuilder internal constructor(private val buildConfig: BuildConfig) {
  fun build(report: CrashReport): String {
    val query = parameters {
      append("template", "bug_report.yml")
      append("title", report.summary.take(MAX_TITLE_LENGTH))
      append("version", report.version)
      append("platform", if (report.platform.startsWith("Android")) "Android" else "Desktop")
      append("crash-report", crashText(report))
    }
    val url = URLBuilder(buildConfig.repoUrl).appendPathSegments("issues", "new")
    url.parameters.appendAll(query)
    return url.buildString()
  }

  private fun crashText(report: CrashReport): String {
    val header =
      """
      |Time: ${report.time}
      |Platform: ${report.platform}
      |Thread: ${report.thread}
      |
      |
      """
        .trimMargin()
    val trace = report.stackTrace
    val clipped =
      if (trace.length <= MAX_TRACE_LENGTH) {
        trace
      } else {
        trace.take(MAX_TRACE_LENGTH).substringBeforeLast('\n') + "\n..."
      }
    return header + clipped
  }

  private companion object {
    const val MAX_TITLE_LENGTH = 120

    // GitHub rejects very long URLs, and every encoded character can take up to 3
    const val MAX_TRACE_LENGTH = 2_500
  }
}
