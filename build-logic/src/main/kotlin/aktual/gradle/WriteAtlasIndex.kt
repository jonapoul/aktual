package aktual.gradle

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Just lists files on disk")
abstract class WriteAtlasIndex : DefaultTask() {
  init {
    group = "atlas"
    description = "Writes an HTML page linking to every module's generated chart"
    doNotTrackState("Charts are found on disk at execution time")
  }

  @get:Internal abstract val rootDir: DirectoryProperty

  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun run() {
    val root = rootDir.get().asFile
    val charts =
      root
        .walkTopDown()
        .onEnter { it == root || it.name !in SKIPPED_DIRS }
        .mapNotNull { dir -> dir.resolve(CHART_PATH).takeIf(File::isFile) }
        .sortedBy { it.modulePath(root) }
        .toList()

    val json =
      charts.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]") { chart ->
        val path = chart.modulePath(root)
        val group = path.split(':').getOrElse(1) { path }
        val fields =
          listOf(
            "\"path\": \"$path\"",
            "\"group\": \"$group\"",
            "\"url\": \"${chart.toPath().toUri()}\"",
            "\"size\": ${chart.length()}",
            "\"modified\": ${chart.lastModified()}",
          )
        fields.joinToString(prefix = "  {", postfix = "}")
      }

    val template = checkNotNull(javaClass.getResource(TEMPLATE)) { "Missing $TEMPLATE" }.readText()
    val output = outputFile.get().asFile
    output.writeText(
      template
        .replace("__GENERATED__", System.currentTimeMillis().toString())
        .replace("__CHARTS__", json),
    )
    logger.lifecycle("Atlas index: ${output.toPath().toUri()}")
  }

  private fun File.modulePath(root: File): String {
    val moduleDir = parentFile.parentFile.parentFile
    return ":" + moduleDir.relativeTo(root).invariantSeparatorsPath.replace('/', ':')
  }

  private companion object {
    const val CHART_PATH = "build/atlas/chart.svg"
    const val TEMPLATE = "/atlas-index.html"
    val SKIPPED_DIRS = setOf("build", "src", ".git", ".gradle", ".idea", "node_modules")
  }
}
