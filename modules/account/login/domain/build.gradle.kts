import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(projects.modules.account.model)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.preferences.core)
    implementation(projects.modules.api.actual)
    implementation(projects.modules.prefs)
    compileOnly(libs.alakazam.kotlin.compose.annotations)
  }

  androidUnitTestDependencies {
    implementation(projects.modules.api.builder)
    implementation(projects.modules.core.connection)
    implementation(projects.modules.test.buildconfig)
    implementation(projects.modules.test.coroutines)
    implementation(projects.modules.test.http)
    implementation(projects.modules.test.prefs)
  }
}
