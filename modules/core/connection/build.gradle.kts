import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(projects.core.buildconfig)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.preferences.core)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.serialization)
    implementation(projects.api.actual)
    implementation(projects.api.builder)
    implementation(projects.core.versions)
    implementation(projects.log)
    implementation(projects.url.model)
    implementation(projects.url.prefs)
  }

  androidUnitTestDependencies {
    implementation(libs.alakazam.android.core)
    implementation(projects.test.buildconfig)
    implementation(projects.test.coroutines)
    implementation(projects.test.prefs)
  }
}
