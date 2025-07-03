plugins {
  alias(libs.plugins.module.hilt)
}

ksp {
  arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(project(":modules:budget:data"))
  implementation(libs.kotlinx.coroutines)
  ksp(libs.dagger.compiler)
}
