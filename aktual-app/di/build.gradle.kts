import aktual.gradle.getOptional
import aktual.gradle.gitVersionCode
import aktual.gradle.gitVersionHash
import aktual.gradle.gitVersionName
import aktual.gradle.localProperties

plugins {
  alias(libs.plugins.module.di)
  alias(libs.plugins.convention.buildconfig)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-api:github"))
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:di"))
    api(project(":aktual-core:model"))
    api(project(":aktual-prefs"))
    api(libs.alakazam.kotlin.core)
    api(libs.alakazam.kotlin.time)
    api(libs.androidx.lifecycle.viewmodel.core)
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(libs.preferences.core)
    implementation(project(":aktual-api:builder"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
  }

  androidMainDependencies {
    implementation(libs.androidx.crypto)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.preferences.android)
  }
}

buildConfig {
  packageName("aktual.app.di")
  useKotlinOutput { topLevelConstants = true }

  buildConfigField("BUILD_TIME_MS", providers.provider { System.currentTimeMillis() })
  buildConfigField("GIT_HASH", gitVersionHash())
  buildConfigField("VERSION_CODE", gitVersionCode())
  buildConfigField("VERSION_NAME", gitVersionName())

  with(rootProject.localProperties()) {
    buildConfigField("DEFAULT_PASSWORD", getOptional("aktual.defaultPassword"))
    buildConfigField("DEFAULT_URL", getOptional("aktual.defaultUrl"))
  }
}
