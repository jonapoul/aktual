package actual.diagrams.tasks

import actual.diagrams.FILENAME_ROOT
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@CacheableTask
open class CheckReadmeTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  val readmeFile: RegularFileProperty = objects.fileProperty()

  @get:Input
  val projectPath: Property<String> = objects.property()

  init {
    group = JavaBasePlugin.VERIFICATION_GROUP
  }

  @TaskAction
  fun execute() {
    val actualContents = readmeFile.get().asFile.readText()
    val expectedTitle = projectPath.get().removePrefix(prefix = ":")
    val expectedContents = """
      # $expectedTitle

      ![$FILENAME_ROOT.png]($FILENAME_ROOT.png)

    """.trimIndent()

    if (expectedContents != actualContents) {
      val msg = """
        Expected contents like:

        '$expectedContents'

        but got contents like:

        '$actualContents'
      """.trimIndent()
      throw GradleException(msg)
    }
  }
}
