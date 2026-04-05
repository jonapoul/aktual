import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.redacted)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.immutable)
    api(libs.kotlinx.serialization.json)
    api(libs.metrox.viewmodel)
    api(libs.okio)
    compileOnly(libs.androidx.compose.annotation)
    compileOnly(libs.redacted.annotations)
  }

  androidMainDependencies { implementation(libs.androidx.coreKtx) }
}
