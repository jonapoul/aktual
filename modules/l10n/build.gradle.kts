import blueprint.core.commonMainDependencies
import dev.jonpoulton.catalog.gradle.CatalogParameterNaming
import dev.jonpoulton.catalog.gradle.GenerateResourcesTask
import dev.jonpoulton.catalog.gradle.NameTransform

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.convention.compose)
  alias(libs.plugins.catalog)
}

android {
  androidResources.enable = true
  buildFeatures.resValues = true
}

catalog {
  generateInternal = false
  parameterNaming = CatalogParameterNaming.ByType
  typePrefix = null
  nameTransform = NameTransform.CamelCase // "resource_name" -> "resourceName"
  generateAtSync = true
}

tasks.register("generateResourceCatalog") {
  dependsOn(tasks.withType<GenerateResourcesTask>())
}

kotlin {
  commonMainDependencies {
    implementation(project.dependencies.platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.core)
  }
}
