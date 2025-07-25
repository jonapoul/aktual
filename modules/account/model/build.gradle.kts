plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.redacted)
}

kotlin {
  commonMainDependencies {
    implementation(libs.alakazam.kotlin.serialization)
    implementation(libs.kotlinx.serialization.core)
    compileOnly(libs.redacted.annotations)
  }
}
