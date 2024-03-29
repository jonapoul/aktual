import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  id("com.google.devtools.ksp")
}

val libs = the<LibrariesForLibs>()
val implementation by configurations
val testImplementation by configurations
val ksp by configurations

dependencies {
  implementation(libs.hilt.core)
  testImplementation(libs.test.hilt)
  ksp(libs.hilt.compiler)
}
