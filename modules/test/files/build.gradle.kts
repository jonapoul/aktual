import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.okio)
    api(libs.test.junit)
    api(libs.test.okio)
    api(projects.budget.model)
  }
}
