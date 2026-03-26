import blueprint.core.commonTestDependencies
import blueprint.core.jvmTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonTestDependencies {
    // utils
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-app:nav:ui"))
    implementation(project(":aktual-budget:model"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-test"))

    // the actual ViewModels
    implementation(project(":aktual-about:vm"))
    implementation(project(":aktual-account:vm"))
    implementation(project(":aktual-budget:list:vm"))
    implementation(project(":aktual-budget:reports:vm"))
    implementation(project(":aktual-budget:sync:vm"))
    implementation(project(":aktual-budget:transactions:vm"))
    implementation(project(":aktual-core:di"))
    implementation(project(":aktual-metrics:vm"))
    implementation(project(":aktual-prefs:vm"))
  }

  jvmTestDependencies { implementation(project(":aktual-app:desktop")) }

  androidHostTestDependencies {
    implementation(project(":aktual-app:android"))
    implementation(libs.metrox.android)
  }
}
