import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.di")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    api(libs.metrox.viewmodel)
  }

  commonTestDependencies {
    implementation(project(":aktual-about:di"))
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-budget:di"))
  }
}
