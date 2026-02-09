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
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.withType

class ConventionDetekt : Plugin<Project> {
  override fun apply(target: Project) =
      with(target) {
        with(plugins) { apply(DetektPlugin::class) }

        extensions.configure<DetektExtension> {
          with(rootProject.isolated.projectDirectory) {
            config.from(file("config/detekt.yml"), file("config/detekt-compose.yml"))
          }
          buildUponDefaultConfig.set(true)
        }

        val detektTasks =
            tasks.withType<Detekt>().matching { task ->
              !task.name.contains("release", ignoreCase = true)
            }

        val detektCheck by tasks.registering { dependsOn(detektTasks) }

        pluginManager.withPlugin("base") { tasks.named("check") { dependsOn(detektCheck) } }

        detektTasks.configureEach {
          reports {
            html.required.set(true)
            sarif.required.set(true)
            checkstyle.required.set(false)
            markdown.required.set(false)
          }
          exclude { node ->
            !node.isDirectory && node.file.absolutePath.contains("generated", ignoreCase = true)
          }
        }

        rootProject.tasks.named("detektReportMergeSarif", ReportMergeTask::class) {
          input.from(detektTasks.map { it.reports.sarif.outputLocation })
        }

        dependencies { "detektPlugins"(libs["detektCompose"]) }
      }
}
