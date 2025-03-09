import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(projects.account.login.domain)
    api(projects.account.model)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.preferences.core)
    implementation(projects.api.actual)
    implementation(projects.log)
  }
}
