import blueprint.core.commonMainDependencies
import blueprint.core.jvmMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
  }

  jvmMainDependencies { api(libs.compose.ui) }
}
