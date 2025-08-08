plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.immutable)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
    compileOnly(libs.alakazam.kotlin.composeAnnotations)
  }

  androidMainDependencies {
    implementation(libs.androidx.coreKtx)
  }
}
