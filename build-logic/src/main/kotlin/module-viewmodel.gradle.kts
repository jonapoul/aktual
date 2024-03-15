import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.the

plugins {
  id("module-android")
  id("convention-hilt")
}

val libs = the<LibrariesForLibs>()
val api by configurations
val implementation by configurations

dependencies {
  api(libs.androidx.lifecycle.viewmodel.ktx)
  api(libs.dagger.core)
  api(libs.javax.inject)
  implementation(libs.hilt.android)
}
