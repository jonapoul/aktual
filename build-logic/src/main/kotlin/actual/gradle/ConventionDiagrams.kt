package actual.gradle

import actual.diagrams.FILENAME
import actual.diagrams.tasks.CalculateProjectTreeTask
import actual.diagrams.tasks.CheckReadmeTask
import actual.diagrams.tasks.CollateModuleTypesTask
import actual.diagrams.tasks.CollateProjectLinksTask
import actual.diagrams.tasks.DumpModuleTypeTask
import actual.diagrams.tasks.DumpProjectLinksTask
import actual.diagrams.tasks.GenerateReadmeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConventionDiagrams : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    if (target == rootProject) {
      CollateProjectLinksTask.register(this)
      CollateModuleTypesTask.register(this)
      return@with
    }

    val realDotFile = layout.projectDirectory.file(FILENAME)

    DumpModuleTypeTask.register(target)
    DumpProjectLinksTask.register(target)
    CalculateProjectTreeTask.register(target)

    GenerateReadmeTask.register(
      target = target,
      name = GenerateReadmeTask.TASK_NAME,
      readme = provider { realDotFile },
      printOutput = true,
    )

    val generateTempReadmeTask = GenerateReadmeTask.register(
      target = target,
      name = "generateTempReadme",
      readme = layout.buildDirectory.file("diagrams-modules-temp/$FILENAME"),
      printOutput = false,
    )

    val checkReadmeTask = CheckReadmeTask.register(target, generateTempReadmeTask, realDotFile)

    tasks.named("check").configure {
      dependsOn(checkReadmeTask)
    }
  }
}
