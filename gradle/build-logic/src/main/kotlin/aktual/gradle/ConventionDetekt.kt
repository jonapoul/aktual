package aktual.gradle

import blueprint.core.get
import blueprint.core.libs
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering

class ConventionDetekt : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(plugins) {
      apply(DetektPlugin::class)
    }

    extensions.configure<DetektExtension> {
      config.setFrom(rootProject.file("config/detekt.yml"))
      buildUponDefaultConfig.set(true)
    }

    val detektTasks = this.detektTasks
    val detektCheck by tasks.registering { dependsOn(detektTasks) }
    tasks.named("check") { dependsOn(detektCheck) }

    detektTasks.configureEach {
      reports {
        html.required.set(true)
        sarif.required.set(true)
        checkstyle.required.set(false)
        markdown.required.set(false)
      }
      exclude { it.file.path.contains("generated") }
    }

    dependencies {
      "detektPlugins"(libs["detektCompose"])
    }
  }
}
