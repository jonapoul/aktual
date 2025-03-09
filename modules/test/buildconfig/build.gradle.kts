import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(projects.core.buildconfig)
    api(libs.kotlinx.datetime)
  }
}
