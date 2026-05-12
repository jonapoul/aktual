package aktual.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ExportMinSdkTask : DefaultTask() {
  init {
    group = "documentation"
    description = "Updates the API level badge in README.md"
  }

  @get:Input abstract val minSdk: Property<Int>

  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val readmeFile: RegularFileProperty

  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun run() {
    val minSdk = minSdk.get()
    val inputFile = readmeFile.asFile.get()
    val outputFile = outputFile.asFile.get()
    val originalContent = inputFile.readText()
    val newContent =
      originalContent
        .replace("API-\\d+%2B".toRegex(), "API-$minSdk%2B")
        .replace("level=\\d+".toRegex(), "level=$minSdk")
    outputFile.writeText(newContent)
    if (originalContent != newContent) {
      throw GradleException("Updated $outputFile with minSdk=$minSdk - you need to commit it!")
    }
  }
}
