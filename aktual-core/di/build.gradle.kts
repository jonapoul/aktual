plugins {
  alias(libs.plugins.module.di)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    api(libs.alakazam.kotlin.time)
    api(libs.androidx.lifecycle.viewmodel.core)
    api(libs.metrox.viewmodel)
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
  }
}
