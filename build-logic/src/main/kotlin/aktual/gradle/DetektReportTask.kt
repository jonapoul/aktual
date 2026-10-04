package aktual.gradle

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.gradle.work.DisableCachingByDefault
import org.w3c.dom.Element
import org.w3c.dom.NodeList

@DisableCachingByDefault(because = "Not worth caching")
abstract class DetektReportTask : DefaultTask() {
  init {
    group = VERIFICATION_GROUP
    description = "Merges the issues of all detekt tasks into one sorted, deduplicated report"
  }

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val checkstyleReports: ConfigurableFileCollection

  // Detekt reports file paths relative to this
  @get:Internal abstract val basePath: DirectoryProperty

  @get:OutputFile abstract val reportFile: RegularFileProperty

  @TaskAction
  fun run() {
    val baseDir = basePath.get().asFile
    val issues =
      checkstyleReports.files
        .filter { it.exists() }
        .flatMap { parse(it, baseDir) }
        .distinct()
        .sortedWith(compareBy({ it.path }, { it.line }, { it.column }, { it.rule }))

    val reportFile = reportFile.get().asFile
    reportFile.writeText(issues.joinToString(separator = "") { "$it\n" })

    if (issues.isNotEmpty()) {
      issues.forEach { logger.error(it.toString()) }
      throw GradleException("Found ${issues.size} detekt issue(s), see file://${reportFile.path}")
    }
  }

  private fun parse(report: File, baseDir: File): List<Issue> {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(report)
    return document.getElementsByTagName("file").elements().flatMap { file ->
      val path = baseDir.resolve(file.getAttribute("name")).normalize().invariantSeparatorsPath
      file.getElementsByTagName("error").elements().map { error ->
        Issue(
          path = path,
          line = error.getAttribute("line").toIntOrNull() ?: 0,
          column = error.getAttribute("column").toIntOrNull() ?: 0,
          rule = error.getAttribute("source").removePrefix("detekt."),
          message = error.getAttribute("message"),
        )
      }
    }
  }

  private fun NodeList.elements(): List<Element> = (0 until length).map { item(it) as Element }

  private data class Issue(
    val path: String,
    val line: Int,
    val column: Int,
    val rule: String,
    val message: String,
  ) {
    // Terminals and the IDE console make a file URL with a line and column clickable
    override fun toString() = "file://$path:$line:$column [$rule] $message"
  }
}
