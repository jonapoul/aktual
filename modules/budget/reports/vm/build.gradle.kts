plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(projects.modules.account.model)
  api(projects.modules.budget.model)
  implementation(libs.alakazam.kotlin.logging)
}
