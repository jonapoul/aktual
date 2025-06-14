package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import com.autonomousapps.DependencyAnalysisPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class ModuleViewModel : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(KotlinAndroidPluginWrapper::class.java)
      apply(ConventionAndroidLibrary::class.java)
      apply(ConventionCompose::class.java)
      apply(ConventionDiagrams::class.java)
      apply(ConventionHilt::class.java)
      apply(ConventionKotlinJvm::class.java)
      apply(ConventionKover::class.java)
      apply(ConventionIdea::class.java)
      apply(ConventionStyle::class.java)
      apply(ConventionTest::class.java)
      apply(DependencyAnalysisPlugin::class.java)
      apply(ConventionSortDependencies::class.java)
    }

    dependencies {
      "api"(libs.getLibrary("androidx.lifecycle.viewmodel.core"))
      "api"(libs.getLibrary("dagger.core"))
      "implementation"(libs.getLibrary("hilt.android"))
      "implementation"(libs.getLibrary("kotlinx.coroutines"))
      "testImplementation"(project(":modules:test:android"))
      "testImplementation"(project(":modules:test:kotlin"))
    }
  }
}
