package actual.gradle

import actual.diagrams.FILENAME_ROOT
import actual.diagrams.tasks.CalculateProjectTreeTask
import actual.diagrams.tasks.CheckDotFileTask
import actual.diagrams.tasks.CollateModuleTypesTask
import actual.diagrams.tasks.CollateProjectLinksTask
import actual.diagrams.tasks.DumpModuleTypeTask
import actual.diagrams.tasks.DumpProjectLinksTask
import actual.diagrams.tasks.GenerateDotFileTask
import actual.diagrams.tasks.GeneratePngFileTask
import actual.diagrams.tasks.WriteReadmeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConventionDiagrams : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    if (target == rootProject) {
      CollateProjectLinksTask.register(this)
      CollateModuleTypesTask.register(this)
      return@with
    }

    val realDotFile = layout.projectDirectory.file("$FILENAME_ROOT.dot")

    DumpModuleTypeTask.register(target)
    DumpProjectLinksTask.register(target)
    CalculateProjectTreeTask.register(target)

    val generateDotFileTask = GenerateDotFileTask.register(
      target = target,
      name = GenerateDotFileTask.TASK_NAME,
      dotFile = provider { realDotFile },
      printOutput = true,
    )

    val generateTempDotFileTask = GenerateDotFileTask.register(
      target = target,
      name = "generateTempDotFile",
      dotFile = layout.buildDirectory.file("diagrams-modules-temp/$FILENAME_ROOT.dot"),
      printOutput = false,
    )

    GeneratePngFileTask.register(target, generateDotFileTask)
    WriteReadmeTask.register(target)

    val checkDotFiles = CheckDotFileTask.register(target, generateTempDotFileTask, realDotFile)

    tasks.named("check").configure {
      dependsOn(checkDotFiles)
    }
  }
}
