package aktual.gradle

import androidUnitTestDependencies
import commonMainDependencies
import commonTestDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

class ModuleMultiplatform : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(KotlinMultiplatformPluginWrapper::class)
      apply(ConventionKotlinBase::class)
      apply(ConventionAndroidLibrary::class)
      apply(ConventionIdea::class)
      apply(ConventionKover::class)
      apply(ConventionStyle::class)
      apply(ConventionTest::class)
    }

    kotlin {
      applyDefaultHierarchyTemplate()

      jvm()
      androidTarget()

      compilerOptions {
        freeCompilerArgs.addAll(
          "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
      }

      commonMainDependencies {
        implementation(libs["kotlinx.coroutines.core"])
      }

      commonTestDependencies {
        testLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":aktual-test:kotlin"))
      }

      androidUnitTestDependencies {
        androidTestLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":aktual-test:android"))
      }
    }
  }
}
