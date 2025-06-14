plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(projects.modules.account.model)
  api(projects.modules.account.password.domain)
  api(projects.modules.core.model)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.javaxInject)
  implementation(projects.modules.account.login.domain)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.modules.core.connection)
}
