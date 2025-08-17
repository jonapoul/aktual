package actual.diagrams.tasks

import actual.diagrams.FILENAME_ROOT
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.ABSOLUTE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class GeneratePngFileTask : DefaultTask() {
  @get:[PathSensitive(ABSOLUTE) InputFile] abstract val dotFile: RegularFileProperty
  @get:OutputFile abstract val pngFile: RegularFileProperty
  @get:OutputFile abstract val errorFile: RegularFileProperty

  @TaskAction
  fun invoke() {
    val dotFile = this.dotFile.get().asFile
    val pngFile = this.pngFile.get().asFile
    val errorFile = this.errorFile.get().asFile

    logger.info("Starting GraphViz...")
    val dotProcess = ProcessBuilder("dot", "-Tpng", dotFile.absolutePath)
      .redirectOutput(pngFile)
      .redirectError(errorFile)
      .start()

    val status = dotProcess.waitFor()
    if (status != 0) {
      throw GradleException("GraphViz error code $status: ${errorFile.bufferedReader().readText()}")
    } else {
      logger.lifecycle(pngFile.absolutePath)
      errorFile.delete()
    }
  }

  companion object {
    const val NAME_MODULES = "generateModulesPng"
    const val NAME_LEGEND = "generateLegendPng"

    fun get(target: Project, name: String) = target.tasks.named<GeneratePngFileTask>(name)

    fun register(target: Project, generateModules: TaskProvider<GenerateModulesDotFileTask>) = with(target) {
      val legend = get(rootProject, NAME_LEGEND)
      tasks.register<GeneratePngFileTask>(NAME_MODULES) {
        group = "reporting"
        dotFile.set(generateModules.map { it.dotFile.get() })
        pngFile.set(layout.projectDirectory.file("$FILENAME_ROOT.png"))
        errorFile.set(layout.projectDirectory.file("$FILENAME_ROOT-error.log"))
        dependsOn(legend)
      }
    }

    fun registerLegend(target: Project, generateLegend: TaskProvider<GenerateLegendDotFileTask>) = with(target) {
      tasks.register<GeneratePngFileTask>(NAME_LEGEND) {
        group = "reporting"
        dotFile.set(generateLegend.map { it.dotFile.get() })
        pngFile.set(layout.projectDirectory.file(GenerateLegendDotFileTask.PNG_PATH))
        errorFile.set(layout.projectDirectory.file("${GenerateLegendDotFileTask.PNG_PATH}.log"))
      }
    }
  }
}
