import org.gradle.kotlin.dsl.kotlin

plugins {
  kotlin("android")
  id("com.android.library")
  id("convention-android-base")
}

android {
  kotlinOptions {
    jvmTarget = "21"
  }

  buildFeatures {
    // Force-disable useless build steps. These can be re-enabled on a per-module basis, if they need them
    androidResources = false
    dataBinding = false
    mlModelBinding = false
    prefabPublishing = false
  }

  packaging {
    resources.excludes.add("META-INF/*")
  }
}
