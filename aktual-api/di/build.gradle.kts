import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.ktor.core)
    api(project(":aktual-api"))
    api(project(":aktual-api:impl"))
    implementation(libs.kotlinx.serialization.json)
  }
}
