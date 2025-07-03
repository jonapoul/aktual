plugins {
  alias(libs.plugins.module.hilt)
}

ksp {
  arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
}

dependencies {
  api(project(":modules:budget:data"))
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  implementation(libs.kotlinx.coroutines)
  ksp(libs.dagger.compiler)
}
