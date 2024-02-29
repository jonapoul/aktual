@file:Suppress("UnstableApiUsage")

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  id("com.google.devtools.ksp")
  id("com.google.dagger.hilt.android")
}

val libs = the<LibrariesForLibs>()
val implementation by configurations
val ksp by configurations

dependencies {
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
}
