plugins {
  alias(libs.plugins.module.kotlin)
}

dependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  testFixturesApi(libs.test.alakazam.core)
}
