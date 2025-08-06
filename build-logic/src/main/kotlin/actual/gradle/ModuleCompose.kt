package actual.gradle

import androidUnitTestDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.getLibrary
import blueprint.core.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ModuleCompose : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class.java)
      apply(ConventionCompose::class.java)
    }

    extensions.configure<KotlinMultiplatformExtension> {
      commonMainDependencies {
        implementation(libs.getLibrary("alakazam.kotlin.compose"))
      }

      commonTestDependencies {
        implementation(project(":modules:test:kotlin"))

        if (name != ":modules:test:compose") {
          implementation(project(":modules:test:compose"))
        }
      }

      androidUnitTestDependencies {
        implementation(project(":modules:test:android"))
      }
    }
  }
}
