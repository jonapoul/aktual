package aktual.gradle

import blueprint.core.javaVersion
import blueprint.core.jvmTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinJvm : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      with(pluginManager) { apply(ConventionKotlinBase::class) }

      tasks.withType<KotlinCompile>().configureEach {
        compilerOptions { jvmTarget.set(jvmTarget()) }
      }

      val javaVersion = javaVersion()
      extensions.configure<JavaPluginExtension> {
        sourceCompatibility = javaVersion.get()
        targetCompatibility = javaVersion.get()
      }
    }
}
