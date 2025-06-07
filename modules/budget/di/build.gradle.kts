plugins {
  alias(libs.plugins.module.hilt)
}

ksp {
  arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(projects.budget.db)
  implementation(libs.kotlinx.coroutines)
  ksp(libs.dagger.compiler)
}
