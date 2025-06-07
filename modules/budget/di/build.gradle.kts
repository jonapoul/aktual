plugins {
  alias(libs.plugins.module.hilt)
}

ksp {
  arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(projects.db)
  ksp(libs.dagger.compiler)
  implementation(libs.kotlinx.coroutines)
}
