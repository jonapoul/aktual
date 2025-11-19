import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:sync:vm"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-l10n"))
  }

  androidMainDependencies {
    implementation(libs.androidx.activity.compose)
  }
}
