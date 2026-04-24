import blueprint.core.commonMainDependencies
import blueprint.core.getOptional
import blueprint.core.gitVersionCode
import blueprint.core.gitVersionDate
import blueprint.core.gitVersionHash
import blueprint.core.localProperties

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    // DI modules
    api(project(":aktual-about:di"))
    api(project(":aktual-budget:data:di"))
    api(project(":aktual-budget:di"))
    api(project(":aktual-core:api:di"))
    api(project(":aktual-core:di"))
    api(project(":aktual-core:model"))
    api(project(":aktual-core:theme:di"))
    api(project(":aktual-prefs:di"))

    // UI modules - needed so Metro discovers @ContributesIntoSet nav entry contributors
    api(project(":aktual-about:ui"))
    api(project(":aktual-account:ui"))
    api(project(":aktual-budget:list:ui"))
    api(project(":aktual-budget:navrail:ui"))
    api(project(":aktual-budget:reports:ui"))
    api(project(":aktual-budget:rules:ui"))
    api(project(":aktual-budget:sync:ui"))
    api(project(":aktual-budget:transactions:ui"))
    api(project(":aktual-metrics:ui"))
    api(project(":aktual-prefs:ui"))
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
