@file:Suppress("UnstableApiUsage")

package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.withType
import blueprint.core.get
import blueprint.core.libs
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke

class ConventionDetekt : ProjectPlugin {
  override fun Project.applyTo() {
    pluginManager.apply(DetektPlugin::class)

    extensions.configure(DetektExtension::class) {
      with(rootProject.isolated.projectDirectory) {
        config.from(
          file("config/detekt.yml"),
          file("config/detekt-compose.yml"),
          file("config/detekt-quiet.yml"),
        )
      }
      baseline.set(file("detekt-baseline.xml"))
      buildUponDefaultConfig.set(true)
      allRules.set(true)
      parallel.set(true)
      debug.set(false)
    }

    val detektTasks = tasks.withType(Detekt::class)
    // Outside build/reports, so CI doesn't annotate the PR with each task's duplicates
    val dekektDir = layout.buildDirectory.dir("detekt")

    val summary =
      gradle.sharedServices.registerIfAbsent(
        DetektSummaryService.NAME,
        DetektSummaryService::class.java,
      ) { spec ->
        val rootDir = rootProject.isolated.projectDirectory
        spec.parameters.reportFile.set(rootDir.file("build/reports/detekt/all-issues.txt"))
      }

    // Each source set and compilation has its own task, so shared sources get checked more than
    // once. Those tasks stay quiet and don't fail, this one reports all their issues instead
    tasks.register("detektCheck", DetektReportTask::class.java) { t ->
      t.dependsOn(detektTasks)
      // Only the enabled tasks, so stale reports of disabled ones are ignored
      t.checkstyleReports.from(
        provider {
          detektTasks.names.filter { isEnabled(it) }.map { dekektDir.get().file("$it.xml") }
        },
      )
      t.basePath.set(rootProject.isolated.projectDirectory)
      t.reportFile.set(layout.buildDirectory.file("reports/detekt/issues.txt"))
      t.summary.set(summary)
      t.usesService(summary)
    }

    detektTasks.configureEach { t ->
      t.enabled = isEnabled(t.name)
      t.ignoreFailures.set(true)

      t.reports { r ->
        r.html.required.set(true)
        r.sarif.required.set(false)
        r.checkstyle.required.set(true)
        r.checkstyle.outputLocation.set(dekektDir.map { it.file("${t.name}.xml") })
        r.markdown.required.set(false)
      }

      t.exclude { node ->
        !node.isDirectory && node.file.absolutePath.contains("generated", ignoreCase = true)
      }
    }

    dependencies { "detektPlugins"(libs["detektCompose"]) }

    if (path != ":detekt-rules") {
      dependencies { "detektPlugins"(project(":detekt-rules")) }
    }
  }

  // The type-resolution tasks per compilation already cover each source set, and the android ones
  // repeat the shared sources, so only keep those when there are android sources
  private fun Project.isEnabled(taskName: String): Boolean {
    val androidSourceSet = ANDROID_COMPILATION_TASKS[taskName]
    return when {
      taskName.contains("release", ignoreCase = true) -> false
      taskName.endsWith("SourceSet") -> false
      androidSourceSet != null -> file("src/$androidSourceSet").exists()
      else -> true
    }
  }

  private companion object {
    val ANDROID_COMPILATION_TASKS =
      mapOf("detektMainAndroid" to "androidMain", "detektHostTestAndroid" to "androidHostTest")
  }
}
