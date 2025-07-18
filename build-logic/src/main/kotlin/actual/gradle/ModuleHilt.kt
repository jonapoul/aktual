package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import com.autonomousapps.DependencyAnalysisPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class ModuleHilt : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(KotlinAndroidPluginWrapper::class.java)
      apply(ConventionAndroidLibrary::class.java)
      apply(ConventionDiagrams::class.java)
      apply(ConventionHilt::class.java)
      apply(ConventionKotlinJvm::class.java)
      apply(ConventionIdea::class.java)
      apply(ConventionStyle::class.java)
      apply(DependencyAnalysisPlugin::class.java)
      apply(ConventionSortDependencies::class.java)
    }

    dependencies {
      "api"(libs.getLibrary("dagger.core"))
      "implementation"(libs.getLibrary("hilt.core"))
    }
  }
}
