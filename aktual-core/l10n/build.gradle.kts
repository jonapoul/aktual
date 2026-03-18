import blueprint.core.commonMainDependencies
import dev.jonpoulton.catalog.gradle.CatalogParameterNaming
import dev.jonpoulton.catalog.gradle.NameTransform
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("aktual.module.kotlin")
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

val catalog = tasks.named("catalog")

tasks.withType(KotlinCompile::class).configureEach { dependsOn(catalog) }

compose.resources {
  generateResClass = always
  nameOfResClass = "Res"
  packageOfResClass = "aktual.core.l10n"
  publicResClass = true
}

kotlin {
  android { androidResources.enable = true }

  commonMainDependencies {
    api(libs.compose.resources)
    api(libs.compose.runtime)
    api(libs.compose.ui)
  }
}
