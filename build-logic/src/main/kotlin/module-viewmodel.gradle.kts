import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.the

plugins {
  id("convention-android-library")
  id("convention-hilt")
  id("convention-style")
  id("convention-test")
  id("com.dropbox.dependency-guard")
}

val libs = the<LibrariesForLibs>()
val api by configurations
val implementation by configurations

dependencies {
  api(libs.androidx.lifecycle.viewmodel.ktx)
  api(libs.dagger.core)
  api(libs.javaxInject)
  implementation(libs.hilt.android)
}

dependencyGuard {
  configuration("debugCompileClasspath")
  configuration("debugRuntimeClasspath")
  configuration("releaseCompileClasspath")
  configuration("releaseRuntimeClasspath")
}
