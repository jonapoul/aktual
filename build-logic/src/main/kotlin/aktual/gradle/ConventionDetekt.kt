@file:Suppress("UnstableApiUsage")

package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.dependencies
import aktual.gradle.dsl.invoke
import aktual.gradle.dsl.withType
import blueprint.core.get
import blueprint.core.libs
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import org.gradle.api.Project

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

    // Each source set and compilation has its own task, so shared sources get checked more than
    // once. Those tasks stay quiet and don't fail, this one reports all their issues instead
    tasks.register("detektCheck", DetektReportTask::class.java) { t ->
      t.dependsOn(detektTasks)
      t.checkstyleReports.from(dekektDir.map { it.asFileTree.matching { f -> f.include("*.xml") } })
      t.basePath.set(rootProject.isolated.projectDirectory)
      t.reportFile.set(layout.buildDirectory.file("reports/detekt/issues.txt"))
    }

    detektTasks.configureEach { t ->
      t.enabled = !t.name.contains("release", ignoreCase = true)
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
}
