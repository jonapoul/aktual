plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.redacted)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.immutable)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
    implementation(libs.alakazam.kotlin.serialization)
    compileOnly(libs.androidx.compose.annotation)
    compileOnly(libs.redacted.annotations)
  }

  androidMainDependencies {
    implementation(libs.androidx.coreKtx)
  }
}
