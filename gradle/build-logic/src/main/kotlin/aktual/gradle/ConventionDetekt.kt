@file:Suppress("UnstableApiUsage")

package aktual.gradle

import blueprint.core.get
import blueprint.core.libs
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import dev.detekt.gradle.report.ReportMergeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType

class ConventionDetekt : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      with(plugins) { apply(DetektPlugin::class) }

      extensions.configure<DetektExtension> {
        with(rootProject.isolated.projectDirectory) {
          config.from(file("config/detekt.yml"), file("config/detekt-compose.yml"))
        }
        baseline.set(file("detekt-baseline.xml"))
        buildUponDefaultConfig.set(true)
        allRules.set(true)
        parallel.set(true)
        debug.set(false)
      }

      val detektTasks = tasks.withType<Detekt>()

      val detektReportMergeSarif =
        rootProject.tasks.named("detektReportMergeSarif", ReportMergeTask::class)

      detektReportMergeSarif.configure {
        input.from(detektTasks.map { it.reports.sarif.outputLocation })
      }

      detektTasks.configureEach {
        enabled = !name.contains("release", ignoreCase = true)

        reports {
          html.required.set(true)
          sarif.required.set(true)
          checkstyle.required.set(false)
          markdown.required.set(false)
        }

        exclude { node ->
          !node.isDirectory && node.file.absolutePath.contains("generated", ignoreCase = true)
        }

        finalizedBy(detektReportMergeSarif)
      }

      dependencies { "detektPlugins"(libs["detektCompose"]) }
    }
}
