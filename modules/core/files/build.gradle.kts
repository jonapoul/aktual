import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.okio)
    api(projects.budget.model)
    implementation(libs.kotlinx.serialization.json)
  }
}
