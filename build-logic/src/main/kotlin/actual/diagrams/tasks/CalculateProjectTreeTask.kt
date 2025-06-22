package actual.diagrams.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class CalculateProjectTreeTask : DefaultTask() {
  @get:[PathSensitive(RELATIVE) InputFile] abstract val collatedLinks: RegularFileProperty
  @get:Input abstract val thisPath: Property<String>
  @get:Input abstract val supportUpwardsTraversal: Property<Boolean>
  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val collatedLinksFile = collatedLinks.get().asFile
    val thisPath = thisPath.get()
    val supportUpwardsTraversal = supportUpwardsTraversal.get()

    val allLinks = ProjectLinks.read(collatedLinksFile)
    val tree = mutableSetOf<ProjectLink>()

    if (supportUpwardsTraversal) {
      calculate(thisPath, Direction.Up, allLinks, tree)
    }
    calculate(thisPath, Direction.Down, allLinks, tree)

    val outputFile = outputFile.get().asFile
    ProjectLinks.write(tree, outputFile)
  }

  private enum class Direction { Up, Down }

  private fun calculate(
    targetPath: String,
    direction: Direction,
    allLinks: Set<ProjectLink>,
    output: MutableSet<ProjectLink>,
  ) {
    when (direction) {
      Direction.Up -> {
        val relevantLinks = allLinks.filter { it.toPath == targetPath }
        output += relevantLinks
        for (link in relevantLinks) {
          calculate(link.fromPath, direction, allLinks, output)
        }
      }

      Direction.Down -> {
        val relevantLinks = allLinks.filter { it.fromPath == targetPath }
        output += relevantLinks
        for (link in relevantLinks) {
          calculate(link.toPath, direction, allLinks, output)
        }
      }
    }
  }

  companion object {
    private const val NAME = "calculateProjectTree"

    fun outputFile(target: Project) = target.fileInReportDirectory("project-links-tree.txt")

    fun register(target: Project) = with(target) {
      tasks.register<CalculateProjectTreeTask>(NAME) {
        group = "reporting"
        thisPath.set(target.path)
        supportUpwardsTraversal.set(boolPropertyProvider("actual.diagram.supportUpwardsTraversal"))
        outputFile.set(outputFile(target))
        collatedLinks.set(CollateProjectLinksTask.outputFile(target))
      }
    }
  }
}
