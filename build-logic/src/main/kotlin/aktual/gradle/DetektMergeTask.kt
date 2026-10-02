package aktual.gradle

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.w3c.dom.Element
import org.w3c.dom.NodeList

@CacheableTask
abstract class DetektMergeTask : DefaultTask() {
  init {
    group = VERIFICATION_GROUP
    description = "Runs all detekt tasks and merges their issues into one deduplicated report"
  }

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val checkstyleReports: ConfigurableFileCollection

  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun run() {
    val issues =
      checkstyleReports.files
        .filter { it.exists() }
        .flatMap(::parse)
        .distinctBy { listOf(it.file, it.line, it.column, it.rule) }
        .sortedWith(compareBy({ it.file }, { it.line }, { it.column }, { it.rule }))

    val report = issues.joinToString(separator = "\n")
    outputFile.get().asFile.writeText(report)

    if (issues.isNotEmpty()) {
      throw GradleException("Found ${issues.size} detekt issue(s):\n$report")
    }
  }

  private fun parse(report: File): List<Issue> {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(report)
    return document.getElementsByTagName("file").elements().flatMap { file ->
      val path = file.getAttribute("name")
      file.getElementsByTagName("error").elements().map { error ->
        Issue(
          file = path,
          line = error.getAttribute("line").toInt(),
          column = error.getAttribute("column").toInt(),
          rule = error.getAttribute("source").removePrefix("detekt."),
          message = error.getAttribute("message"),
        )
      }
    }
  }

  private fun NodeList.elements(): List<Element> = (0 until length).map { item(it) as Element }

  private data class Issue(
    val file: String,
    val line: Int,
    val column: Int,
    val rule: String,
    val message: String,
  ) {
    override fun toString() = "$file:$line:$column: [$rule] $message"
  }
}
