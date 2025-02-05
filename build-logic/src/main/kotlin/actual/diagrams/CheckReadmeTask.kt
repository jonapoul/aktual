package actual.diagrams

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class CheckReadmeTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
  @get:InputFile
  val readmeFile: RegularFileProperty = objects.fileProperty()

  @TaskAction
  fun execute() {
    val actualContents = readmeFile.get().asFile.readText()
    val expectedTitle = project.path.removePrefix(prefix = ":")
    val expectedContents = """
      # $expectedTitle

      ![project-dependency-graph.png](project-dependency-graph.png)

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
