import aktual.gradle.versionName
import blueprint.core.gitVersionCode
import blueprint.core.gitVersionHash
import blueprint.core.stringPropertyOrNull

plugins {
  alias(libs.plugins.module.di)
  alias(libs.plugins.buildconfig)
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

android {
  buildFeatures {
    buildConfig = true
  }
}

buildConfig {
  packageName("aktual.app.di")
  generateAtSync.set(true)

  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }

  val url = stringPropertyOrNull(key = "aktual.defaultUrl")
  val password = stringPropertyOrNull(key = "aktual.defaultPassword")

  buildConfigField("BUILD_TIME_MS", System.currentTimeMillis())
  buildConfigField("GIT_HASH", gitVersionHash())
  buildConfigField("VERSION_CODE", gitVersionCode())
  buildConfigField("VERSION_NAME", versionName())
  buildConfigField<String?>("DEFAULT_PASSWORD", password)
  buildConfigField<String?>("DEFAULT_URL", url)
}
