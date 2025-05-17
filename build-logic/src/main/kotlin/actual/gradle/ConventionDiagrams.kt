package actual.gradle

import actual.diagrams.FILENAME_ROOT
import actual.diagrams.tasks.CheckDotFileTask
import actual.diagrams.tasks.CheckReadmeTask
import actual.diagrams.tasks.GenerateDotFileTask
import actual.diagrams.tasks.GeneratePngFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class ConventionDiagrams : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    val path = project.path
    val removeModulePrefix = ":modules:"
    val replacementModulePrefix = ":"

    val realDotFile = project.layout.projectDirectory.file("$FILENAME_ROOT.dot")

    val generateDotFileTask = tasks.register<GenerateDotFileTask>(GenerateDotFileTask.TASK_NAME) {
      description = "Generates a project dependency graph for $path"
      dotFile.set(realDotFile)
      toRemove.set(removeModulePrefix)
      replacement.set(replacementModulePrefix)
      printOutput.set(true)
    }

    val generateTempDotFileTask = tasks.register<GenerateDotFileTask>("generateTempDotFile") {
      dotFile.set(project.layout.buildDirectory.file("diagrams-modules-temp/$FILENAME_ROOT.dot"))
      toRemove.set(removeModulePrefix)
      replacement.set(replacementModulePrefix)
      printOutput.set(false)
    }

    tasks.register<CheckDotFileTask>("checkDotFiles") {
      taskPath.set("$path:${GenerateDotFileTask.TASK_NAME}")
      expectedDotFile.set(generateTempDotFileTask.get().dotFile)
      actualDotFile.set(realDotFile)
    }

    tasks.register<GeneratePngFileTask>("generateModulesPng") {
      val reportDir = layout.projectDirectory
      dotFile.set(generateDotFileTask.get().dotFile)
      pngFile.set(reportDir.file("$FILENAME_ROOT.png"))
      errorFile.set(reportDir.file("$FILENAME_ROOT-error.log"))
    }

    val checkModulesReadmeTask = tasks.register<CheckReadmeTask>("checkModulesReadme") {
      readmeFile.set(file("README.md"))
      projectPath.set(path)
    }

    tasks.named("check").configure {
      dependsOn(checkModulesReadmeTask)
    }
  }
}
