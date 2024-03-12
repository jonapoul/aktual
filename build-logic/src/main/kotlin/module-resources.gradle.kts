@file:Suppress("UnstableApiUsage")

plugins {
  id("com.android.library")
  id("convention-android-base")
  id("convention-licensee")
  id("convention-spotless")
}

android {
  buildFeatures {
    androidResources = true
    resValues = true
  }
}
