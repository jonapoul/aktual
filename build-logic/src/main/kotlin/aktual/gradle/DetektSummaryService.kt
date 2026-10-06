package aktual.gradle

import java.util.concurrent.ConcurrentHashMap
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

// Collects the issues of every detektCheck task in the build, then prints them as one sorted,
// deduplicated list when the build finishes
abstract class DetektSummaryService : BuildService<DetektSummaryService.Params>, AutoCloseable {
  interface Params : BuildServiceParameters {
    val reportFile: RegularFileProperty
  }

  private val issues = ConcurrentHashMap.newKeySet<DetektIssue>()

  internal fun add(issues: Collection<DetektIssue>) {
    this.issues += issues
  }

  override fun close() {
    val sorted = issues.sorted()
    val reportFile = parameters.reportFile.get().asFile
    reportFile.parentFile.mkdirs()
    reportFile.writeText(sorted.joinToString(separator = "") { "$it\n" })
    if (sorted.isEmpty()) return

    val logger = Logging.getLogger(DetektSummaryService::class.java)
    logger.error("\nFound ${sorted.size} detekt issue(s), see file://${reportFile.path}")
    sorted.forEach { logger.error(it.toString()) }
  }

  companion object {
    const val NAME = "detektSummary"
  }
}

internal data class DetektIssue(
  val path: String,
  val line: Int,
  val column: Int,
  val rule: String,
  val message: String,
) : Comparable<DetektIssue> {
  override fun compareTo(other: DetektIssue) = ORDER.compare(this, other)

  // Terminals and the IDE console make a file URL with a line and column clickable
  override fun toString() = "file://$path:$line:$column [$rule] $message"

  private companion object {
    val ORDER = compareBy<DetektIssue>({ it.path }, { it.line }, { it.column }, { it.rule })
  }
}
