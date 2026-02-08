import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.compose")
}

kotlin {
  commonMainDependencies {
    api(libs.jetbrains.ui)
  }
}
