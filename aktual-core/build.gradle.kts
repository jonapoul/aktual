import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(libs.compose.runtime)
    api(libs.okio)
    api(project(":aktual-core:model"))
    api(project(":aktual-di:core"))
  }

  androidMainDependencies {
    implementation(libs.androidx.core)
  }
}
