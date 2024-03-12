import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  id("com.google.devtools.ksp")
}

val libs = the<LibrariesForLibs>()
val implementation by configurations
val ksp by configurations

dependencies {
  implementation(libs.hilt.core)
  ksp(libs.hilt.compiler)
}
