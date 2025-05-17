package actual.diagrams.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
open class GeneratePngFileTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  val dotFile: RegularFileProperty = objects.fileProperty()

  @get:OutputFile
  val pngFile: RegularFileProperty = objects.fileProperty()

  @get:OutputFile
  val errorFile: RegularFileProperty = objects.fileProperty()

  init {
    group = "reporting"
  }

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
}
