package aktual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

class ModuleJvm : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      with(pluginManager) {
        apply(KotlinPluginWrapper::class)
        apply(ConventionKotlinJvm::class)
        apply(ConventionIdea::class)
        apply(ConventionStyle::class)
        apply(ConventionTest::class)
      }

      dependencies { testLibraries.forEach { lib -> "testImplementation"(lib) } }
    }
}
