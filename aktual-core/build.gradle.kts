import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.compose.runtime)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    api(libs.okio)
    api(project(":aktual-core:model"))
    api(project(":aktual-di:core"))
  }

  androidMainDependencies {
    implementation(libs.androidx.core)
  }
}
