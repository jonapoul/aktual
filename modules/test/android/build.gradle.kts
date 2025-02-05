plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.kotlinx.datetime)
  implementation(libs.test.androidx.coreKtx)
}
