package actual.gradle

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
      apply(ConventionAndroidLibrary::class.java)
      apply(ConventionDiagrams::class.java)
      apply(ConventionKover::class.java)
      apply(ConventionIdea::class.java)
      apply(ConventionStyle::class.java)
      apply(ConventionTest::class.java)
//      apply(DependencyAnalysisPlugin::class.java) // doesn't support KMP
//      apply(ConventionSortDependencies::class.java) // doesn't support KMP
    }

    extensions.configure<KotlinMultiplatformExtension> {
      compilerOptions {
        freeCompilerArgs.addAll(FREE_COMPILER_ARGS)
        freeCompilerArgs.add("-Xexpect-actual-classes")
      }

      jvm()
      androidTarget()

      commonTestDependencies {
        testLibraries.forEach { lib -> implementation(lib) }
      }
    }
  }
}
