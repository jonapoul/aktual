plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    implementation(project(":aktual-api:actual"))
  }
}
