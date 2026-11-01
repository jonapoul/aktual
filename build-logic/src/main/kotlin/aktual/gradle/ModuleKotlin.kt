package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.kotlin
import aktual.gradle.dsl.testLibraries
import blueprint.core.get
import blueprint.core.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.commonMainDependencies
import org.gradle.kotlin.dsl.commonTestDependencies
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm

class ModuleKotlin : ProjectPlugin {
  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  override fun Project.applyTo() {
    with(pluginManager) {
      apply(KotlinMultiplatformPluginWrapper::class)
      apply(ConventionKotlinBase::class)
      apply(ConventionIdea::class)
      apply(ConventionStyle::class)
      apply(ConventionTest::class)
    }

    kotlin {
      applyDefaultHierarchyTemplate {
        common {
          group("jvmShared") {
            withJvm()
            withCompilations { it.platformType == androidJvm }
          }
        }
      }

      jvm("desktop")

      compilerOptions {
        freeCompilerArgs.addAll("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
      }

      commonMainDependencies {
        implementation(libs["alakazam.kotlin"])
        implementation(libs["kotlinx.coroutines.core"])
      }

      commonTestDependencies {
        implementation(kotlin("test"))
        testLibraries.forEach { lib -> implementation(lib) }
      }
    }
  }
}
