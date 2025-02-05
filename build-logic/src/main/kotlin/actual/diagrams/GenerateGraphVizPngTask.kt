package actual.diagrams

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class GenerateGraphVizPngTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
  @get:InputDirectory
  val reportDir: DirectoryProperty = objects.directoryProperty()

  @get:InputFile
  val dotFile: RegularFileProperty = objects.fileProperty()

  @get:OutputFile
  val pngFile: RegularFileProperty = objects.fileProperty()

  @get:OutputFile
  val errorFile: RegularFileProperty = objects.fileProperty()

  init {
    // never cache
    outputs.upToDateWhen { false }
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
      logger.info("GraphViz success!")
      errorFile.delete()
    }
  }
}
