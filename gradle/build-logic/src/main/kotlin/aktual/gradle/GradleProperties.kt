package aktual.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun Project.javaVersion(): Provider<JavaVersion> = javaVersionString().map(JavaVersion::toVersion)

fun Project.jvmTarget(): Provider<JvmTarget> = javaVersionString().map(JvmTarget::fromTarget)

private fun Project.javaVersionString(): Provider<String> =
  providers.of(JavaVersionValueSource::class.java) {
    parameters.javaVersionFile.set(rootProject.layout.projectDirectory.file(".java-version"))
  }

private abstract class JavaVersionValueSource : ValueSource<String, JavaVersionValueSource.Parameters> {
  interface Parameters : ValueSourceParameters {
    @get:InputFile
    @get:PathSensitive(NONE)
    val javaVersionFile: RegularFileProperty
  }

  override fun obtain(): String {
    val file = parameters.javaVersionFile.asFile.get()
    require(file.isFile) { "Java version file does not exist: ${file.absolutePath}" }

    val content = file.readText().trim()
    require(content.isNotEmpty()) { "Java version file is empty: ${file.absolutePath}" }

    requireNotNull(content.toIntOrNull()) { "Java version must be a valid integer, but was: $content" }

    return content
  }
}
