import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(projects.account.model)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.preferences.core)
    implementation(projects.api.actual)
    implementation(projects.prefs)
    compileOnly(libs.alakazam.kotlin.compose.annotations)
  }

  androidUnitTestDependencies {
    implementation(projects.api.builder)
    implementation(projects.core.connection)
    implementation(projects.test.buildconfig)
    implementation(projects.test.coroutines)
    implementation(projects.test.http)
    implementation(projects.test.prefs)
  }
}
