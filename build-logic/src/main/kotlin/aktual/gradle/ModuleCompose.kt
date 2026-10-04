package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.composeLibraries
import aktual.gradle.dsl.kotlin
import blueprint.core.get
import blueprint.core.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.androidHostTestDependencies
import org.gradle.kotlin.dsl.androidMainDependencies
import org.gradle.kotlin.dsl.commonMainDependencies
import org.gradle.kotlin.dsl.commonTestDependencies

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

        if (project.path != ":aktual-test:compose") {
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
