package actual.diagrams.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class CollateProjectLinksTask : DefaultTask() {
  @get:[PathSensitive(RELATIVE) InputFiles] abstract val projectLinkFiles: ConfigurableFileCollection
  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val outputFile = outputFile.get().asFile
    val allLinks = mutableSetOf<ProjectLink>()
    projectLinkFiles.forEach { file -> allLinks += ProjectLinks.read(file) }
    ProjectLinks.write(allLinks, outputFile)
  }

  companion object {
    private const val NAME = "collateProjectLinks"

    fun get(target: Project) = target.tasks.named<CollateProjectLinksTask>(NAME)

    fun register(target: Project) = with(target) {
      if (target != rootProject) {
        error("Should only ever register CollateProjectLinksTask on root project, not $path")
      }

      val task = tasks.register<CollateProjectLinksTask>(NAME) {
        group = "reporting"
        outputFile.set(fileInReportDirectory("project-links-all.txt"))
      }

      gradle.projectsEvaluated {
        val dumpTasks = rootProject.subprojects.mapNotNull(DumpProjectLinksTask::get)
        task.configure {
          dependsOn(dumpTasks)
          projectLinkFiles.from(dumpTasks.map { it.get().outputFile })
        }
      }

      task
    }
  }
}
