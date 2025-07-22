package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.withType

class ConventionDetekt : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(plugins) {
      apply(DetektPlugin::class)
    }

    extensions.getByType(DetektExtension::class).apply {
      config.setFrom(rootProject.file("config/detekt.yml"))
      buildUponDefaultConfig = true
    }

    val check by tasks.getting
    val detektTasks = tasks.withType<Detekt>()
    val detektCheck by tasks.registering

    detektCheck.configure { dependsOn(detektTasks) }

    detektTasks.configureEach {
      reports.html.required.set(true)
      exclude { it.file.path.contains("generated") }

      if (!name.contains("release", ignoreCase = true)) {
        check.dependsOn(this)
      }
    }

    dependencies {
      "detektPlugins"(libs.getLibrary("plugin.detektCompose"))
    }
  }
}
