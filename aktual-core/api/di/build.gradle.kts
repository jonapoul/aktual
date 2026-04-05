import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.di")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.ktor.core)
    api(project(":aktual-core:api"))
    api(project(":aktual-core:api:impl"))
    implementation(project(":aktual-prefs"))
  }
}
