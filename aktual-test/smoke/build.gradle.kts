plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-app:nav"))

    implementation(project(":aktual-about:vm"))
    implementation(project(":aktual-account:vm"))
    implementation(project(":aktual-budget:list:vm"))
    implementation(project(":aktual-budget:reports:vm"))
    implementation(project(":aktual-budget:sync:vm"))
    implementation(project(":aktual-budget:transactions:vm"))
    implementation(project(":aktual-metrics:vm"))
    implementation(project(":aktual-settings:vm"))
  }

  jvmTestDependencies {
    implementation(project(":aktual-app:desktop"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-app:android"))
    implementation(libs.metrox.android)
    implementation(libs.preferences.android)
  }
}
