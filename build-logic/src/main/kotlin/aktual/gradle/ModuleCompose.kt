@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package aktual.gradle

import androidHostTestDependencies
import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.get
import blueprint.core.jvmMainDependencies
import blueprint.core.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

class ModuleCompose : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      with(pluginManager) {
        apply(ModuleKotlin::class)
        apply(ConventionCompose::class)
      }

      kotlin {
        commonMainDependencies {
          api(libs["compose.runtime"])
          implementation(libs["alakazam.compose"])
          implementation(libs["androidx.lifecycle.runtime.compose"])
          implementation(libs["androidx.lifecycle.viewmodel.compose"])
          implementation(libs["compose.animation"])
          implementation(libs["compose.foundation"])
          implementation(libs["compose.material3"])
          implementation(libs["compose.ui"])
          implementation(libs["compose.uiToolingPreview"])
          implementation(libs["compose.uiUtil"])
          implementation(libs["kotlinx.immutable"])
          implementation(libs["metrox.viewmodel.compose"])
        }

        commonTestDependencies {
          implementation(project(":aktual-test"))

          if (name != ":aktual-test:compose") {
            implementation(project(":aktual-test:compose"))
          }
        }

        androidMainDependencies { implementation(libs["androidx.poolingcontainer"]) }

        androidHostTestDependencies { implementation(libs["androidx.test.composeJunit4"]) }

        jvmMainDependencies {
          implementation(extensions.getByType<ComposePlugin.Dependencies>().desktop.currentOs)
        }
      }

      dependencies { "androidRuntimeClasspath"(libs["compose.uiTooling"]) }
    }
}
