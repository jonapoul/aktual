plugins {
  alias(libs.plugins.module.di)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    api(libs.androidx.lifecycle.viewmodel.core)
    implementation(libs.kotlinx.coroutines.core)
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
  }
}
