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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

class ConventionDetekt : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.apply(DetektPlugin::class)

      extensions.configure(DetektExtension::class) {
        with(rootProject.isolated.projectDirectory) {
          config.from(file("config/detekt.yml"), file("config/detekt-compose.yml"))
        }
        baseline.set(file("detekt-baseline.xml"))
        buildUponDefaultConfig.set(true)
        allRules.set(true)
        parallel.set(true)
        debug.set(false)
      }

      val detektTasks = tasks.withType(Detekt::class)

      tasks.register("detektCheck") { t ->
        t.group = VERIFICATION_GROUP
        t.dependsOn(detektTasks)
      }

      detektTasks.configureEach { t ->
        t.enabled = !t.name.contains("release", ignoreCase = true)

        t.reports { r ->
          r.html.required.set(true)
          r.sarif.required.set(false)
          r.checkstyle.required.set(false)
          r.markdown.required.set(false)
        }

        t.exclude { node ->
          !node.isDirectory && node.file.absolutePath.contains("generated", ignoreCase = true)
        }
      }

      dependencies { "detektPlugins"(libs["detektCompose"]) }

      if (target.path != ":detekt-rules") {
        dependencies { "detektPlugins"(project(":detekt-rules")) }

        extensions.configure(DetektExtension::class) {
          config.from(rootProject.isolated.projectDirectory.file("config/detekt-aktual.yml"))
        }
      }
    }
}
