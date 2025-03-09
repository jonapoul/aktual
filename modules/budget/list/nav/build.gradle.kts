import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.navigation)
}

kotlin {
  commonMainDependencies {
    api(projects.account.model)
  }
}
