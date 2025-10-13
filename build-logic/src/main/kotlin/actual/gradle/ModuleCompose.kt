@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package actual.gradle

import androidUnitTestDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.invoke
import blueprint.core.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

class ModuleCompose : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class)
      apply(ConventionCompose::class)
    }

    kotlin {
      sourceSets {
        invokeWhenCreated("androidDebug") {
          dependencies {
            implementation(compose.preview)
          }
        }
      }

      commonMainDependencies {
        api(compose.runtime)
        implementation(compose.animation)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.materialIconsExtended)
        implementation(compose.ui)
        implementation(compose.uiTooling)
        implementation(compose.uiUtil)
        implementation(libs("alakazam.kotlin.compose"))
        implementation(libs("androidx.lifecycle.runtime.compose"))
        implementation(libs("androidx.lifecycle.viewmodel.compose"))
        implementation(libs("kotlinx.immutable"))
      }

      commonTestDependencies {
        implementation(project(":modules:test:kotlin"))

        if (name != ":modules:test:compose") {
          implementation(project(":modules:test:compose"))
        }
      }

      androidUnitTestDependencies {
        implementation(project(":modules:test:android"))
        implementation(libs("test.androidx.compose.ui.junit4"))
      }
    }
  }
}
