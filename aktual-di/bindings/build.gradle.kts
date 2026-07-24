import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":aktual-budget"))
    api(project(":aktual-core"))
    implementation(libs.ktor.cio)
    implementation(project(":aktual-di:core"))
  }
}
