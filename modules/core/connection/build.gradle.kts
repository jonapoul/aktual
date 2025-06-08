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
    implementation(projects.modules.api.actual)
    implementation(projects.modules.api.builder)
    implementation(projects.modules.core.model)
    implementation(projects.modules.prefs)
  }

  androidUnitTestDependencies {
    implementation(libs.alakazam.android.core)
    implementation(projects.modules.test.buildconfig)
    implementation(projects.modules.test.coroutines)
    implementation(projects.modules.test.http)
    implementation(projects.modules.test.prefs)
  }
}
