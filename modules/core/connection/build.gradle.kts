import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.alakazam.kotlin.core)
    implementation(libs.alakazam.kotlin.logging)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.preferences.core)
    implementation(projects.api.actual)
    implementation(projects.api.builder)
    implementation(projects.core.model)
    implementation(projects.url.prefs)
  }

  androidUnitTestDependencies {
    implementation(libs.alakazam.android.core)
    implementation(projects.test.buildconfig)
    implementation(projects.test.coroutines)
    implementation(projects.test.http)
    implementation(projects.test.prefs)
  }
}
