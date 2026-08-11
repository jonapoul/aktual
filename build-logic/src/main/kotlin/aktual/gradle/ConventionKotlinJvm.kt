package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.withType
import blueprint.core.javaVersion
import blueprint.core.jvmTarget
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinJvm : ProjectPlugin {
  override fun Project.applyTo() {
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
