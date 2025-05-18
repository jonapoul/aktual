package actual.diagrams.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault
open class CheckDotFileTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
  @get:Input val taskPath: Property<String> = objects.property()
  @get:InputFile val expectedDotFile: RegularFileProperty = objects.fileProperty()
  @get:InputFile val actualDotFile: RegularFileProperty = objects.fileProperty()

  init {
    group = JavaBasePlugin.VERIFICATION_GROUP

    // never cache
    outputs.cacheIf { false }
  }

  @TaskAction
  fun execute() {
    val expectedContents = expectedDotFile.get().asFile.readLines()
    val actualContents = actualDotFile.get().asFile.readLines()

    require(expectedContents == actualContents) {
      """
        Dotfile needs updating! Run `gradle ${taskPath.get()}` to regenerate ${expectedDotFile.get()}.

        Expected (len = ${expectedContents.size}):
        '${expectedContents.joinToString("\n")}'

        Actual (len = ${actualContents.size}):
        '${actualContents.joinToString("\n")}'
      """.trimIndent()
    }
  }
}
