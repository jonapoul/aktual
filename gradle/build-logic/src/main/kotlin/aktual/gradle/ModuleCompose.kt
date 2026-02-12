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
        apply(ModuleMultiplatform::class)
        apply(ConventionCompose::class)
      }

      kotlin {
        commonMainDependencies {
          api(libs["jetbrains.runtime"])
          implementation(libs["alakazam.compose"])
          implementation(libs["androidx.lifecycle.runtime.compose"])
          implementation(libs["androidx.lifecycle.viewmodel.compose"])
          implementation(libs["jetbrains.animation"])
          implementation(libs["jetbrains.foundation"])
          implementation(libs["jetbrains.material3"])
          implementation(libs["jetbrains.ui"])
          implementation(libs["jetbrains.uiToolingPreview"])
          implementation(libs["jetbrains.uiUtil"])
          implementation(libs["kotlinx.immutable"])
          implementation(libs["metrox.viewmodel.compose"])
        }

        commonTestDependencies {
          implementation(project(":aktual-test:kotlin"))

          if (name != ":aktual-test:compose") {
            implementation(project(":aktual-test:compose"))
          }
        }

        androidMainDependencies { implementation(libs["androidx.poolingcontainer"]) }

        androidHostTestDependencies {
          implementation(project(":aktual-test:android"))
          implementation(libs["test.androidx.compose.ui.junit4"])
        }

        jvmMainDependencies {
          implementation(extensions.getByType<ComposePlugin.Dependencies>().desktop.currentOs)
        }
      }

      dependencies { "androidRuntimeClasspath"(libs["jetbrains.uiTooling"]) }
    }
}
