package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.withType
import blueprint.core.javaVersion
import blueprint.core.jvmTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinJvm : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      with(pluginManager) { apply(ConventionKotlinBase::class) }

      tasks.withType(KotlinCompile::class).configureEach { t ->
        t.compilerOptions { jvmTarget.set(jvmTarget()) }
      }

      val javaVersion = javaVersion()
      extensions.configure(JavaPluginExtension::class) {
        sourceCompatibility = javaVersion.get()
        targetCompatibility = javaVersion.get()
      }
    }
}
