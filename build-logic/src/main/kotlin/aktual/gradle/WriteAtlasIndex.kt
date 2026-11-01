package aktual.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Just lists files on disk")
abstract class WriteAtlasIndex : DefaultTask() {
  init {
    group = "atlas"
    description = "Writes an HTML page linking to every module's generated chart"
    doNotTrackState("Always prints the page's URL")
  }

  @get:Input abstract val chartIds: ListProperty<ComponentArtifactIdentifier>

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.ABSOLUTE)
  abstract val chartFiles: ConfigurableFileCollection

  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val projectTypes: RegularFileProperty

  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun run() {
    val charts =
      chartIds
        .get()
        .map { (it.componentIdentifier as ProjectComponentIdentifier).projectPath }
        .zip(chartFiles.files)
        .sortedBy { (path, _) -> path }

    val json =
      charts.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]") { (path, chart) ->
        val fields =
          listOf(
            "\"path\": \"$path\"",
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
        .replace("__CHARTS__", json)
        .replace("__TYPES__", projectTypes.get().asFile.readText()),
    )
    logger.lifecycle("Atlas index: ${output.toPath().toUri()}")
  }

  private companion object {
    const val TEMPLATE = "/atlas-index.html"
  }
}
