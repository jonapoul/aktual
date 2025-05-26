import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.json)
    api(projects.api.actual)
  }
}
