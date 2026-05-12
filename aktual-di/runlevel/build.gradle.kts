import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-di:graphs"))
    api(libs.alakazam.kotlin)
  }
}
