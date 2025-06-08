plugins {
  alias(libs.plugins.module.jvm)
}

dependencies {
  api(libs.ksp.api)
  implementation(libs.kotlinpoet.core)
  implementation(libs.kotlinpoet.ksp)
  implementation(projects.modules.codegen.annotation)
}
