plugins {
  alias(libs.plugins.module.di)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:data"))
    api(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
  }
}
