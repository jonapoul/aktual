package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.stringProperty
import com.android.build.api.dsl.LibraryExtension
import com.autonomousapps.DependencyAnalysisPlugin
import dev.jonpoulton.catalog.gradle.CatalogExtension
import dev.jonpoulton.catalog.gradle.CatalogParameterNaming
import dev.jonpoulton.catalog.gradle.CatalogPlugin
import dev.jonpoulton.catalog.gradle.NameTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class ModuleResources : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(KotlinAndroidPluginWrapper::class.java)
      apply(ConventionAndroidLibrary::class.java)
      apply(ConventionCompose::class.java)
      apply(ConventionDiagrams::class.java)
      apply(ConventionLicensee::class.java)
      apply(ConventionSpotless::class.java)
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

    val resourcePrefix = stringProperty(key = "resource.prefix")

    extensions.configure<CatalogExtension> {
      generateInternal = false
      parameterNaming = CatalogParameterNaming.ByType

      // "Strings" -> "PrefixedStrings" for prefix of "prefixed"
      typePrefix = resourcePrefix.capitalized()

      // "prefixed_resource_name" -> "resourceName" for prefix of "prefixed"
      nameTransform = NameTransform.chained(
        NameTransform.removePrefix(prefix = resourcePrefix),
        NameTransform.removePrefix(prefix = "_"),
        NameTransform.CamelCase,
      )
    }

    val implementation by configurations
    val api by configurations

    dependencies {
      // Required for resource accessor generation
      implementation(platform(libs.getLibrary("androidx.compose.bom")))
      api(libs.getLibrary("androidx.compose.ui.core"))
    }
  }
}
