import blueprint.core.getOptional
import blueprint.core.gitVersionCode
import blueprint.core.gitVersionDate
import blueprint.core.gitVersionHash
import blueprint.core.localProperties

plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.prefs)
    api(libs.androidx.datastore.prefsCore)
    api(libs.compose.resources)
    api(libs.kotlinx.coroutines.core)
    api(libs.ktor.core)
    api(libs.sqldelight.runtime)
    api(project(":aktual-about:data"))
    api(project(":aktual-api:impl"))
    api(project(":aktual-budget:banksync:impl"))
    api(project(":aktual-budget:budgeting:impl"))
    api(project(":aktual-budget:data:impl"))
    api(project(":aktual-budget:payees:impl"))
    api(project(":aktual-budget:schedules:impl"))
    api(project(":aktual-budget:sync"))
    api(project(":aktual-budget:tags:impl"))
    api(project(":aktual-budget:transactions:impl"))
    api(project(":aktual-core:l10n"))
    api(project(":aktual-core:theme:impl"))
    api(project(":aktual-di:bindings"))
    api(project(":aktual-di:runlevel:impl"))
    api(project(":aktual-prefs:budget"))
    api(project(":aktual-prefs:impl"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okio)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
  }
}

buildConfig {
  packageName("aktual.app.di")
  useKotlinOutput { topLevelConstants = true }

  buildConfigField("BUILD_TIME_MS", providers.provider { System.currentTimeMillis() })
  buildConfigField("GIT_HASH", gitVersionHash())
  buildConfigField("VERSION_CODE", gitVersionCode())
  buildConfigField("VERSION_NAME", gitVersionDate())

  with(localProperties()) {
    buildConfigField("DEFAULT_PASSWORD", getOptional("aktual.defaultPassword"))
    buildConfigField("DEFAULT_URL", getOptional("aktual.defaultUrl"))
  }
}
