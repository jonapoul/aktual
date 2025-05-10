import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.preferences.core)
    api(projects.account.model)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
    implementation(projects.api.actual)
    compileOnly(libs.alakazam.kotlin.compose.annotations)
  }

  androidUnitTestDependencies {
    implementation(projects.api.builder)
    implementation(projects.core.connection)
    implementation(projects.test.buildconfig)
    implementation(projects.test.coroutines)
    implementation(projects.test.http)
    implementation(projects.test.prefs)
    implementation(projects.url.prefs)
  }
}
