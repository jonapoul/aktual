plugins { id("aktual.module.jvm") }

dependencies {
  api(libs.ksp.api)
  implementation(project(":aktual-codegen:annotation"))
  implementation(libs.kotlinpoet.core)
  implementation(libs.kotlinpoet.ksp)
}
