package aktual.gradle

import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonTestDependencies
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
      apply(ConventionTest::class)
    }

    kotlin {
      jvm()
      androidTarget()

      commonTestDependencies {
        testLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":aktual-test:di"))
        implementation(project(":aktual-test:kotlin"))
      }

      androidUnitTestDependencies {
        androidTestLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":aktual-test:android"))
      }
    }
  }
}
