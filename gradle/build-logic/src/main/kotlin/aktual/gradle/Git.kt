package aktual.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

fun Project.gitVersionHash(): Provider<String> = providers.of(GitVersionHashValueSource::class.java) {}

fun Project.gitVersionCode(): Provider<Int> = providers.of(GitVersionCodeValueSource::class.java) {}

fun Project.gitVersionName(): Provider<String> = providers.of(GitVersionNameValueSource::class.java) {}

private abstract class GitVersionHashValueSource : ValueSource<String, ValueSourceParameters.None> {
  @get:Inject abstract val execOperations: ExecOperations

  override fun obtain(): String {
    val output = ByteArrayOutputStream()
    execOperations.exec {
      commandLine("git", "rev-parse", "--short=8", "HEAD")
      standardOutput = output
    }
    return output.toString().trim()
  }
}

private abstract class GitVersionCodeValueSource : ValueSource<Int, ValueSourceParameters.None> {
  @get:Inject abstract val execOperations: ExecOperations

  override fun obtain(): Int {
    val output = ByteArrayOutputStream()
    execOperations.exec {
      commandLine("git", "show", "-s", "--format=%ct")
      standardOutput = output
    }
    return output.toString().trim().toInt()
  }
}

private abstract class GitVersionNameValueSource : ValueSource<String, ValueSourceParameters.None> {
  @get:Inject abstract val execOperations: ExecOperations

  override fun obtain(): String {
    val output = ByteArrayOutputStream()
    execOperations.exec {
      commandLine("git", "show", "-s", "--format=%ct")
      standardOutput = output
    }
    val timestamp = output.toString().trim().toLong()
    val date = Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.UTC).toLocalDate()
    return "%04d.%02d.%02d".format(date.year, date.monthValue, date.dayOfMonth)
  }
}
