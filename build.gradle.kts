import blueprint.core.rootLocalPropertiesOrNull
import blueprint.recipes.dependencyVersionsBlueprint

plugins {
  alias(libs.plugins.agp.app) apply false
  alias(libs.plugins.agp.lib) apply false
  alias(libs.plugins.androidCacheFix) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.burst) apply false
  alias(libs.plugins.catalog) apply false
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.composeHotReload) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.powerAssert) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.licensee) apply false
  alias(libs.plugins.manifestLock) apply false
  alias(libs.plugins.metro) apply false
  alias(libs.plugins.redacted) apply false
  alias(libs.plugins.sqldelight) apply false

  alias(libs.plugins.dependencyGuard)
  alias(libs.plugins.dependencyVersions)
  alias(libs.plugins.doctor)
  alias(libs.plugins.kover)

  alias(libs.plugins.convention.diagrams)
  alias(libs.plugins.convention.idea)
  alias(libs.plugins.convention.kover)
}

// Place all local properties in the project-level gradle properties map
rootLocalPropertiesOrNull()?.forEach { (key, value) ->
  ext[key.toString()] = value.toString()
}

doctor {
  javaHome {
    ensureJavaHomeMatches = false
    ensureJavaHomeIsSet = true
    failOnError = true
  }
}

dependencyGuard {
  configuration("classpath")
}

dependencyVersionsBlueprint()
