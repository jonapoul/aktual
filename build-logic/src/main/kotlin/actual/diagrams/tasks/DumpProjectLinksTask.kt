package actual.diagrams.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class DumpProjectLinksTask : DefaultTask() {
  @get:Input abstract val projectDependencies: MapProperty<String, List<String>>
  @get:Input abstract val thisPath: Property<String>
  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun execute() {
    ProjectLinks.write(
      outputFile = outputFile.get().asFile,
      fromPath = thisPath.get(),
      projectDependencies = projectDependencies.get(),
    )
  }

  companion object {
    private const val NAME = "dumpProjectLinks"

    fun outputFile(target: Project) = target.fileInReportDirectory("project-links-this.txt")

    fun register(target: Project) = with(target) {
      tasks.register<DumpProjectLinksTask>(NAME) {
        group = "reporting"
        thisPath.set(target.path)
        projectDependencies.set(ProjectLinks.of(target))
        outputFile.set(outputFile(target))
      }
    }
  }
}
