import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    api(libs.ktor.core)
    implementation(libs.ktor.cio)
  }
}
