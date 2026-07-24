import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget"))
    api(project(":aktual-core"))
    api(libs.ktor.core)
    implementation(project(":aktual-di:core"))
    implementation(libs.ktor.cio)
  }
}
