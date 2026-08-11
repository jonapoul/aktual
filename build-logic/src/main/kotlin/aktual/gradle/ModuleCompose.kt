@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package aktual.gradle

import aktual.gradle.dsl.androidHostTestDependencies
import aktual.gradle.dsl.apply
import aktual.gradle.dsl.composeLibraries
import aktual.gradle.dsl.kotlin
import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.get
import blueprint.core.libs
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

class ModuleCompose : ProjectPlugin {
  override fun Project.applyTo() {
    with(pluginManager) {
      apply(ModuleKotlin::class)
      apply(ConventionCompose::class)
    }

    kotlin {
      commonMainDependencies {
        api(libs["compose.runtime"])
        composeLibraries.forEach { implementation(it) }
      }

      commonTestDependencies {
        implementation(project(":aktual-test"))

        if (name != ":aktual-test:compose") {
          implementation(project(":aktual-test:compose"))
        }
      }

      androidMainDependencies {
        implementation(libs["androidx.poolingcontainer"])
      }

      androidHostTestDependencies {
        implementation(libs["androidx.test.composeJunit4"])
      }
    }
  }
}
