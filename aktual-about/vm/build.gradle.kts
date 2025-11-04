plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-about:data"))
    api(libs.alakazam.kotlin.core)
  }
}
