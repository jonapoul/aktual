@file:Suppress("TooManyFunctions")

package aktual.gradle

import dev.detekt.gradle.Detekt
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFilter
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileNotFoundException
import java.time.LocalDate
import java.util.Properties

val Project.detektTasks: TaskCollection<Detekt>
  get() = tasks
    .withType<Detekt>()
    .matching { task -> !task.name.contains("release", ignoreCase = true) }

const val EXPERIMENTAL_MATERIAL_3 = "androidx.compose.material3.ExperimentalMaterial3Api"

fun Project.optIn(klass: String) = optIn(listOf(klass))

fun Project.optIn(vararg classes: String) = optIn(classes.toList())

fun Project.optIn(classes: Collection<String>) {
  kotlin { compilerOptions.freeCompilerArgs.addAll(classes.map { "-opt-in=$it" }) }
}

fun Project.koverExcludes(config: Action<KoverReportFilter>) = extensions.configure<KoverProjectExtension> {
  reports.total.filters { excludes(config) }
}

fun versionName(): String = with(LocalDate.now()) { "%04d.%02d.%02d".format(year, monthValue, dayOfMonth) }

fun Project.javaVersionInt(): Int = intProperty(key = "aktual.javaVersion").get()
fun Project.javaVersionString(): String = stringProperty(key = "aktual.javaVersion").get()
fun Project.javaVersion(): JavaVersion = JavaVersion.toVersion(javaVersionInt())
fun Project.jvmTarget(): JvmTarget = JvmTarget.fromTarget(javaVersionString())

fun Project.stringProperty(key: String): Provider<String> = providers.gradleProperty(key)
fun Project.intProperty(key: String): Provider<Int> = stringProperty(key).map(String::toInt)
fun Project.boolProperty(key: String): Provider<Boolean> = stringProperty(key).map(String::toBoolean)

fun Project.gitVersionHash(): String = execCommand("git", "rev-parse", "--short=8", "HEAD")
fun Project.gitVersionCode(): Int = execCommand("git", "show", "-s", "--format=%ct").toInt()

fun Project.execCommand(vararg args: String): String = providers
  .exec { commandLine(*args) }
  .standardOutput
  .asText
  .get()
  .trim('\n', ' ')

private const val DEFAULT_FILENAME = "local.properties"

fun Project.localProperties(filename: String = DEFAULT_FILENAME): Properties {
  val file = projectDir.resolve(filename)
  if (!file.exists() || !file.isFile) {
    throw FileNotFoundException("No properties file found at ${file.absolutePath}")
  }
  val props = Properties()
  file.reader().use { props.load(it) }
  return props
}

fun Project.localPropertiesOrNull(filename: String = DEFAULT_FILENAME): Properties? = try {
  localProperties(filename)
} catch (e: FileNotFoundException) {
  logger.warn(e.toString())
  null
}
