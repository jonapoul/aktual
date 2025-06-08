import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(projects.modules.account.login.domain)
    api(projects.modules.account.model)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.alakazam.kotlin.logging)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.preferences.core)
    implementation(projects.modules.api.actual)
    implementation(projects.modules.prefs)
  }
}
