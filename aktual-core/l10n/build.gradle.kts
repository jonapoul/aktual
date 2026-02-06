import blueprint.core.commonMainDependencies
import dev.jonpoulton.catalog.gradle.CatalogParameterNaming
import dev.jonpoulton.catalog.gradle.NameTransform

plugins {
  id("aktual.module.multiplatform")
  id("aktual.convention.compose")
  alias(libs.plugins.catalog)
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
  packageOfResClass = "aktual.core.l10n"
  publicResClass = true
}

kotlin {
  android {
    androidResources.enable = true
  }

  commonMainDependencies {
    api(libs.jetbrains.resources)
    api(libs.jetbrains.runtime)
    api(libs.jetbrains.ui)
  }
}
