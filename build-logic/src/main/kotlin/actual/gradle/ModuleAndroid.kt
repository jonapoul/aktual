package actual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class ModuleAndroid : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(KotlinAndroidPluginWrapper::class)
      apply(ConventionAndroidLibrary::class)
      apply(ConventionKover::class)
      apply(ConventionKotlinJvm::class)
      apply(ConventionIdea::class)
      apply(ConventionStyle::class)
      apply(ConventionTest::class)
    }

    dependencies {
      "testImplementation"(project(":modules:test:android"))
      "testImplementation"(project(":modules:test:kotlin"))
    }
  }
}
