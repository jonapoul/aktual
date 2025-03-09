package actual.gradle

import blueprint.core.javaVersion
import blueprint.core.jvmTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinJvm : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionKotlinBase::class.java)
    }

    tasks.withType<KotlinCompile> {
      compilerOptions {
        jvmTarget.set(project.jvmTarget())
      }
    }

    val javaVersion = javaVersion()
    extensions.configure<JavaPluginExtension> {
      sourceCompatibility = javaVersion
      targetCompatibility = javaVersion
    }
  }
}
