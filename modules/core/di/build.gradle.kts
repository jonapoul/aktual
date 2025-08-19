plugins {
  alias(libs.plugins.module.di)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:data"))
    api(project(":modules:budget:model"))
    api(libs.androidx.lifecycle.viewmodel.core)
    implementation(libs.kotlinx.coroutines)
  }

  commonTestDependencies {
    implementation(project(":app:di"))
  }
}
