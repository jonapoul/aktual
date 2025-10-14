package actual.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.ByteArrayOutputStream

fun Project.gitVersionCode(): Int = providers
  .gradleProperty("actual.versionCode")
  .orElse(execCommand("git", "show", "-s", "--format=%ct"))
  .map { it.toInt() }
  .get()

fun Project.gitVersionHash(): Provider<String> = providers
  .gradleProperty("actual.versionHash")
  .orElse(execCommand("git", "rev-parse", "--short=8", "HEAD"))

private fun Project.execCommand(vararg args: String): Provider<String> {
  val argsList = args.toList()
  val error = ByteArrayOutputStream()
  val output = ByteArrayOutputStream()

  val execOutput = providers.exec {
    commandLine(argsList)
    this.errorOutput = error
    this.standardOutput = output
  }

  return execOutput
    .result
    .map { result ->
      if (result.exitValue != 0) error("Failed running '$argsList': ${result.exitValue}. output=$output, error=$error")
      output.toString().trim('\n', ' ')
    }
}
