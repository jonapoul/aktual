plugins {
  alias(libs.plugins.agp.app) apply false
  alias(libs.plugins.agp.kmp) apply false
  alias(libs.plugins.androidCacheFix) apply false
  alias(libs.plugins.blueprint) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.burst) apply false
  alias(libs.plugins.catalog) apply false
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.licensee) apply false
  alias(libs.plugins.manifestLock) apply false
  alias(libs.plugins.metro) apply false
  alias(libs.plugins.redacted) apply false
  alias(libs.plugins.sortDependencies) apply false
  alias(libs.plugins.sqldelight) apply false
  alias(libs.plugins.straitjacket) apply false
  alias(libs.plugins.testRetry) apply false
  alias(libs.plugins.wire) apply false

  id("aktual.convention.idea")
}
