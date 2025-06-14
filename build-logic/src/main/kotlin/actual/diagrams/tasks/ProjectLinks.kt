package actual.diagrams.tasks

import okio.buffer
import okio.sink
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Provider
import java.io.File

internal object ProjectLinks {
  fun of(project: Project): Provider<Map<String, List<String>>> = project.provider {
    val map = hashMapOf<String, List<String>>()
    project
      .configurations
      .filterUseful()
      .forEach { c ->
        c.dependencies
          .filterIsInstance<ProjectDependency>()
          .forEach { module ->
            map[module.path] = map.getOrElse(module.path) { listOf<String>() } + c.name
          }
      }
    map
  }

  fun read(inputFile: File): Set<ProjectLink> = inputFile
    .readLines()
    .map(::ProjectLink)
    .toSet()

  fun write(links: Collection<ProjectLink>, outputFile: File) {
    outputFile.sink().buffer().use { sink ->
      links.sorted().forEach { link ->
        sink.writeUtf8(link.toString())
        sink.writeUtf8("\n")
      }
    }
  }

  fun write(outputFile: File, fromPath: String, projectDependencies: Map<String, List<String>>) {
    outputFile.writeText(
      buildString {
        projectDependencies
          .toSortedMap()
          .forEach { (toPath, configurations) ->
            configurations
              .sorted()
              .forEach { configuration -> appendLine("$fromPath $toPath $configuration") }
          }
      },
    )
  }
}

internal data class ProjectLink(
  val fromPath: String,
  val toPath: String,
  val configuration: String,
) : Comparable<ProjectLink> {
  private fun Int.nullIfZero(): Int? = if (this == 0) null else this

  override fun compareTo(other: ProjectLink): Int =
    fromPath.compareTo(other.fromPath).nullIfZero()
      ?: toPath.compareTo(other.toPath).nullIfZero()
      ?: configuration.compareTo(other.configuration)

  override fun toString(): String = "$fromPath $toPath $configuration"
}

private fun ProjectLink(line: String): ProjectLink {
  val (fromPath, toPath, configuration) = line.split(" ")
  return ProjectLink(fromPath, toPath, configuration)
}

private val BLOCKED_CONFIGS = setOf(
  "debug",
  "kover",
  "ksp",
  "test",
)

private fun ConfigurationContainer.filterUseful() = filter { c ->
  BLOCKED_CONFIGS.none { blocked ->
    c.name.contains(blocked, ignoreCase = true)
  }
}
