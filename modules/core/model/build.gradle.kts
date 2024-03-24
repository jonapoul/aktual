plugins {
  id("module-kotlin")
  alias(libs.plugins.sekret)
}

dependencies {
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  compileOnly(libs.sekret)
}
