@file:Suppress("UnstableApiUsage")

package aktual.gradle

import com.android.build.api.dsl.Lint
import org.gradle.api.Project

fun Lint.commonConfigure(project: Project) {
  abortOnError = true
  checkGeneratedSources = false
  checkReleaseBuilds = false
  checkReleaseBuilds = false
  checkTestSources = true
  explainIssues = true
  htmlReport = true
  xmlReport = true
  lintConfig = project.rootProject.isolated.projectDirectory.file("config/lint.xml").asFile
}

// ":aktual-path:to:module" -> "aktual.path.to.module", or ":aktual-app:android" ->
// "aktual.app.android"
internal fun Project.buildNamespace() =
  path.split(":", "-").filter { it.isNotBlank() }.joinToString(".")
