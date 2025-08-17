package actual.diagrams.tasks

import actual.diagrams.ModuleType
import actual.diagrams.color
import actual.diagrams.label
import okio.buffer
import okio.sink
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class GenerateLegendDotFileTask : DefaultTask() {
  @get:OutputFile abstract val dotFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val dotFileContents = buildString {
      appendLine("digraph G {")
      appendLine("node [shape=plaintext]")
      appendLine("table1 [label=<")
      appendLine("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">")
      ModuleType.entries.forEach { type ->
        appendLine("<TR><TD>${type.label}</TD><TD BGCOLOR=\"${type.color.value}\">module-name</TD></TR>")
      }
      appendLine("</TABLE>")
      appendLine(">];")
      appendLine("}")
    }

    dotFile.get()
      .asFile
      .sink()
      .buffer()
      .use { sink -> sink.writeUtf8(dotFileContents) }
  }

  companion object {
    const val TASK_NAME = "generateLegendDotFile"
    const val DOT_PATH = "docs/legend/legend.dot"
    const val PNG_PATH = "docs/legend/legend.png"

    fun get(target: Project) = target.tasks.named<GenerateLegendDotFileTask>(TASK_NAME)

    fun register(target: Project) = with(target) {
      tasks.register<GenerateLegendDotFileTask>(TASK_NAME) {
        group = "reporting"
        description = "Generates a project dependency graph for $path"
        dotFile.set(provider { target.layout.projectDirectory.file(DOT_PATH) })
      }
    }
  }
}
