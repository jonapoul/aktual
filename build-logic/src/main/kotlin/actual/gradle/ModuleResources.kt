package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import com.android.build.api.dsl.LibraryExtension
import com.autonomousapps.DependencyAnalysisPlugin
import dev.jonpoulton.catalog.gradle.CatalogExtension
import dev.jonpoulton.catalog.gradle.CatalogParameterNaming
import dev.jonpoulton.catalog.gradle.CatalogPlugin
import dev.jonpoulton.catalog.gradle.GenerateResourcesTask
import dev.jonpoulton.catalog.gradle.NameTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class ModuleResources : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(KotlinAndroidPluginWrapper::class.java)
      apply(ConventionAndroidLibrary::class.java)
      apply(ConventionCompose::class.java)
      apply(ConventionDiagrams::class.java)
      apply(ConventionIdea::class.java)
      apply(ConventionLicensee::class.java)
      apply(CatalogPlugin::class.java)
      apply(DependencyAnalysisPlugin::class.java)
      apply(ConventionSortDependencies::class.java)
    }

    extensions.configure<LibraryExtension> {
      buildFeatures {
        androidResources = true
        resValues = true
      }
    }

    extensions.configure<CatalogExtension> {
      generateInternal = false
      parameterNaming = CatalogParameterNaming.ByType
      typePrefix = null
      nameTransform = NameTransform.CamelCase // "resource_name" -> "resourceName"

      // Regen the accessors when syncing the IDE
      generateAtSync = true
    }

    tasks.register("generateResourceCatalog") {
      // TODO: There's probably a way to make this configure lazily but I can't figure it out
      dependsOn(tasks.withType<GenerateResourcesTask>())
    }

    dependencies {
      // Required for resource accessor generation
      "implementation"(platform(libs.getLibrary("androidx.compose.bom")))
      "api"(libs.getLibrary("androidx.compose.runtime"))
      "api"(libs.getLibrary("androidx.compose.ui.core"))
    }
  }
}
