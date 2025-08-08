import dev.jonpoulton.catalog.gradle.CatalogParameterNaming
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

compose.resources {
  generateResClass = always
  nameOfResClass = "Res"
  packageOfResClass = "actual.l10n"
  publicResClass = true
}

kotlin {
  commonMainDependencies {
    api(compose.components.resources)
    api(compose.runtime)
    api(compose.ui)
  }
}
