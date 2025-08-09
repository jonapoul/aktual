plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:about:data"))
    api(libs.alakazam.kotlin.core)
  }
}
