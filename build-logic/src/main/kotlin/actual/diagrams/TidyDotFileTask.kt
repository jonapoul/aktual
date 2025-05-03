package actual.diagrams

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class TidyDotFileTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
  @get:InputFile
  val dotFile: RegularFileProperty = objects.fileProperty()

  @get:Input
  val toRemove: Property<String> = objects.property(String::class.java)

  @get:Input
  val replacement: Property<String> = objects.property(String::class.java)

  init {
    // never cache
    outputs.upToDateWhen { false }
  }

  @TaskAction
  fun invoke() {
    val file = this.dotFile.get().asFile

    logger.info("Removing modules prefix from dotfile at $file...")

    val fileContents = file
      .readText()
      .replace(toRemove.get(), replacement.get())
      .withoutUnusedModuleTypes()
      .withSortedModuleDeclarations()
      .withSortedModuleLinks()
      .withoutEmptyLines()

    file
      .writer()
      .buffered()
      .use { writer -> writer.write(fileContents) }
  }

  private fun String.withoutUnusedModuleTypes(): String = buildString {
    val usedColours = mutableSetOf<String>()
    val moduleTypeColorsAppended = mutableSetOf<String>()

    val lines = this@withoutUnusedModuleTypes.lineSequence()
    for (line in lines) {
      val fillColorMatch = FILL_COLOR_REGEX.matchEntire(line)
      if (fillColorMatch != null) {
        val color = fillColorMatch.groupValues[1]
        usedColours.add(color)
      }

      val tableLineMatch = TABLE_LINE_REGEX.matchEntire(line)
      if (tableLineMatch != null) {
        val color = tableLineMatch.groupValues[1]
        if (color in usedColours && color !in moduleTypeColorsAppended) {
          appendLine(line)
          moduleTypeColorsAppended.add(color)
        }
        continue
      }

      appendLine(line)
    }
  }

  private fun String.withSortedModuleDeclarations(): String = buildString {
    val lines = this@withSortedModuleDeclarations.lineSequence()
    val moduleDeclarations = mutableSetOf<String>()
    var isInModuleDeclarations = false

    for (line in lines) {
      if (line.startsWith("\":") && line.contains("fillcolor") && line.contains("box")) {
        isInModuleDeclarations = true
        moduleDeclarations.add(line)
      } else if (isInModuleDeclarations) {
        // we were in declarations before but not now, so print them all sorted
        isInModuleDeclarations = false
        for (module in moduleDeclarations.sorted()) {
          appendLine(module)
        }
      }

      if (!isInModuleDeclarations) {
        appendLine(line)
      }
    }

    appendLine()
  }

  private fun String.withSortedModuleLinks(): String = buildString {
    val lines = this@withSortedModuleLinks.lineSequence()
    val linkDeclarations = mutableSetOf<String>()
    var isInLinkDeclarations = false

    for (line in lines) {
      if (line.startsWith("\":") && line.contains("->")) {
        isInLinkDeclarations = true
        linkDeclarations.add(line)
      } else if (isInLinkDeclarations) {
        // we were in declarations before but not now, so print them all sorted
        isInLinkDeclarations = false
        for (module in linkDeclarations.sorted()) {
          appendLine(module)
        }
      }

      if (!isInLinkDeclarations) {
        appendLine(line)
      }
    }

    appendLine()
  }

  private fun String.withoutEmptyLines(): String = buildString {
    for (line in this@withoutEmptyLines.lineSequence()) {
      if (line.isNotEmpty()) appendLine(line)
    }
  }

  private companion object {
    val FILL_COLOR_REGEX = """
      ^.*?"fillcolor"="#(\w+)".*?$
    """.trimIndent().toRegex()

    val TABLE_LINE_REGEX = """
      ^<TR><TD>.*?</TD><TD BGCOLOR="#(.*?)">module-name</TD></TR>$
    """.trimIndent().toRegex()
  }
}
