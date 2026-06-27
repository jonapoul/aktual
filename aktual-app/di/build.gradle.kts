import blueprint.core.commonMainDependencies
import blueprint.core.getOptional
import blueprint.core.gitVersionCode
import blueprint.core.gitVersionDate
import blueprint.core.gitVersionHash
import blueprint.core.localProperties

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.prefs)
    api(libs.ktor.core)
    api(libs.sqldelight.runtime)
    api(project(":aktual-about:data"))
    api(project(":aktual-budget:data:impl"))
    api(project(":aktual-budget:sync:domain"))
    api(project(":aktual-core:api:impl"))
    api(project(":aktual-core:logging:impl"))
    api(project(":aktual-core:theme:impl"))
    api(project(":aktual-di:bindings"))
    api(project(":aktual-di:runlevel:impl"))
    api(project(":aktual-prefs:impl"))
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
