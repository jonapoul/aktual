package actual.diagrams.tasks

import actual.diagrams.FILENAME_ROOT
import okio.buffer
import okio.sink
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class WriteReadmeTask : DefaultTask() {
  @get:Input abstract val projectPath: Property<String>
  @get:Input abstract val legendPngRelativePath: Property<String>
  @get:OutputFile abstract val readmeFile: RegularFileProperty

  @TaskAction
  fun action() {
    val legendPngRelativePath = legendPngRelativePath.get()
    val expectedTitle = projectPath.get().removePrefix(prefix = ":")

    val contents = buildString {
      appendLine("# $expectedTitle")
      appendLine("![modules]($FILENAME_ROOT.png)")
      appendLine("![legend]($legendPngRelativePath)")
    }

    readmeFile.asFile
      .get()
      .sink()
      .buffer()
      .use { sink -> sink.writeUtf8(contents) }
  }

  companion object {
    fun register(target: Project) = with(target) {
      val removeModulePrefix = providers.gradleProperty("actual.diagram.removeModulePrefix")
      val legendPng = rootProject.file(GenerateLegendDotFileTask.PNG_PATH)
      val projectDir = target.layout.projectDirectory.asFile
      val legendTask = GenerateLegendDotFileTask.get(rootProject)
      tasks.register<WriteReadmeTask>("writeReadme") {
        group = "reporting"
        readmeFile.set(file("README.md"))
        projectPath.set(target.path.removePrefix(removeModulePrefix.get()))
        legendPngRelativePath.set(legendPng.relativeTo(projectDir).toString())
        dependsOn(legendTask)
      }
    }
  }
}
