@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package aktual.gradle

import androidUnitTestDependencies
import commonMainDependencies
import commonTestDependencies
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
            implementation(libs["jetbrains.preview"])
          }
        }
      }

      commonMainDependencies {
        api(libs["jetbrains.runtime"])
        implementation(libs["alakazam.kotlin.compose"])
        implementation(libs["androidx.lifecycle.runtime.compose"])
        implementation(libs["androidx.lifecycle.viewmodel.compose"])
        implementation(libs["jetbrains.animation"])
        implementation(libs["jetbrains.foundation"])
        implementation(libs["jetbrains.material3"])
        implementation(libs["jetbrains.materialIcons"])
        implementation(libs["jetbrains.ui"])
        implementation(libs["jetbrains.uiTooling"])
        implementation(libs["jetbrains.uiUtil"])
        implementation(libs["kotlinx.immutable"])
      }

      commonTestDependencies {
        implementation(project(":aktual-test:kotlin"))

        if (name != ":aktual-test:compose") {
          implementation(project(":aktual-test:compose"))
        }
      }

      androidUnitTestDependencies {
        implementation(project(":aktual-test:android"))
        implementation(libs["test.androidx.compose.ui.junit4"])
      }
    }
  }
}
