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
    implementation(libs.alakazam.kotlin.logging)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.preferences.core)
    implementation(projects.modules.api.actual)
    implementation(projects.modules.prefs)
    compileOnly(libs.alakazam.kotlin.compose.annotations)
  }

  androidUnitTestDependencies {
    implementation(projects.modules.api.builder)
    implementation(projects.modules.core.connection)
  }
}
