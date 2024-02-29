import com.lordcodes.turtle.ShellFailedException
import com.lordcodes.turtle.ShellRunException
import com.lordcodes.turtle.shellRun
import org.gradle.api.Project

fun Project.gitVersionName(): String {
  return runGitCommandOrNull(args = listOf("rev-parse", "--short=8", "HEAD"))
    ?: error("Failed getting git version from ${project.path}")
}

fun Project.runGitCommandOrNull(command: String = "git", args: List<String>): String? {
  return try {
    shellRun(
      workingDirectory = rootProject.rootDir,
      command = command,
      arguments = args,
    )
  } catch (e: ShellFailedException) {
    logger.error("Shell failed trying to run $command $args", e)
    null
  } catch (e: ShellRunException) {
    logger.error("Command failed running $command $args", e)
    null
  }
}
