import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.ktor.core)
    api(libs.metrox.viewmodel)
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    implementation(libs.ktor.cio)
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:data-di"))
    implementation(project(":aktual-prefs:di"))
    implementation(project(":aktual-test"))
  }
}
