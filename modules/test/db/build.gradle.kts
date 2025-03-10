import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.test.alakazam.core)
    api(libs.test.androidx.coreKtx)
    api(libs.test.kotlinx.coroutines)
    api(projects.db)
    implementation(libs.alakazam.db.sqldelight)
    implementation(libs.sqldelight.driver.sqlite)
  }

  androidMainDependencies {
    implementation(libs.sqldelight.driver.android)
  }
}
