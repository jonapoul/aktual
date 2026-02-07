import blueprint.core.commonMainDependencies
import blueprint.core.getOptional
import blueprint.core.gitVersionCode
import blueprint.core.gitVersionDate
import blueprint.core.gitVersionHash
import blueprint.core.localProperties

plugins {
  id("aktual.module.di")
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.androidx.lifecycle.viewmodel.core)
    api(libs.metrox.viewmodel)
    api(libs.preferences.core)
    api(project(":aktual-about:di"))
    api(project(":aktual-budget:data-di"))
    api(project(":aktual-core:di"))
    api(project(":aktual-core:model"))
    api(project(":aktual-core:prefs-di"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(project(":aktual-core:api"))
  }
}

buildConfig {
  packageName("aktual.app.di")
  useKotlinOutput { topLevelConstants = true }

  buildConfigField("BUILD_TIME_MS", providers.provider { System.currentTimeMillis() })
  buildConfigField("GIT_HASH", providers.gitVersionHash())
  buildConfigField("VERSION_CODE", providers.gitVersionCode())
  buildConfigField("VERSION_NAME", providers.gitVersionDate())

  with(localProperties()) {
    buildConfigField("DEFAULT_PASSWORD", getOptional("aktual.defaultPassword"))
    buildConfigField("DEFAULT_URL", getOptional("aktual.defaultUrl"))
  }
}
