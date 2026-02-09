package aktual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class ModuleAndroid : Plugin<Project> {
  override fun apply(target: Project) =
      with(target) {
        with(pluginManager) {
          apply(ConventionAndroidLibrary::class)
          apply(ConventionKover::class)
          apply(ConventionKotlinJvm::class)
          apply(ConventionIdea::class)
          apply(ConventionStyle::class)
          apply(ConventionTest::class)
        }

        dependencies {
          "testImplementation"(project(":aktual-test:android"))
          "testImplementation"(project(":aktual-test:kotlin"))
        }
      }
}
