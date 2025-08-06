package actual.gradle

import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonTestDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

class ModuleMultiplatform : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(KotlinMultiplatformPluginWrapper::class.java)
      apply(ConventionKotlinBase::class.java)
      apply(ConventionAndroidLibrary::class.java)
      apply(ConventionDiagrams::class.java)
      apply(ConventionKover::class.java)
      apply(ConventionIdea::class.java)
      apply(ConventionStyle::class.java)
      apply(ConventionTest::class.java)
    }

    extensions.configure<KotlinMultiplatformExtension> {
      jvm()
      androidTarget()

      commonTestDependencies {
        testLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":modules:test:kotlin"))
      }

      androidUnitTestDependencies {
        androidTestLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":modules:test:android"))
      }
    }
  }
}
