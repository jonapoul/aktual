import aktual.gradle.gitVersionCode
import aktual.gradle.gitVersionHash
import aktual.gradle.localPropertiesOrNull
import aktual.gradle.versionName

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

buildConfig {
  packageName("aktual.app.di")
  generateAtSync = true

  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }

  val localProps = rootProject.localPropertiesOrNull()
  val url = localProps?.get("aktual.defaultUrl")?.toString()
  val password = localProps?.get("aktual.defaultPassword")?.toString()

  buildConfigField("BUILD_TIME_MS", System.currentTimeMillis())
  buildConfigField("GIT_HASH", gitVersionHash())
  buildConfigField("VERSION_CODE", gitVersionCode())
  buildConfigField("VERSION_NAME", versionName())
  buildConfigField<String?>("DEFAULT_PASSWORD", password)
  buildConfigField<String?>("DEFAULT_URL", url)
}
