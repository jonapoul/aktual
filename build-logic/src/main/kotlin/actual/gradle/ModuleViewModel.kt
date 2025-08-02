package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import com.autonomousapps.DependencyAnalysisPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class ModuleViewModel : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(KotlinAndroidPluginWrapper::class)
      apply(ConventionAndroidLibrary::class)
      apply(ConventionCompose::class)
      apply(ConventionDiagrams::class)
      apply(ConventionKotlinJvm::class)
      apply(ConventionKover::class)
      apply(ConventionIdea::class)
      apply(ConventionStyle::class)
      apply(ConventionTest::class)
      apply(DependencyAnalysisPlugin::class)
      apply(ConventionSortDependencies::class)
    }

    dependencies {
      "api"(project(":modules:core:di"))
      "api"(libs.getLibrary("androidx.lifecycle.viewmodel.core"))
      "implementation"(libs.getLibrary("kotlinx.coroutines"))
      "implementation"(libs.getLibrary("molecule"))
      "implementation"(project(":modules:logging"))
      "testImplementation"(project(":modules:test:android"))
      "testImplementation"(project(":modules:test:kotlin"))
    }
  }
}
