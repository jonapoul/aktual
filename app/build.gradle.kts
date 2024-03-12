plugins {
  kotlin("android")
  alias(libs.plugins.agp.app)
  id("convention-compose")
  id("convention-desugaring")
  id("com.google.dagger.hilt.android")
  id("convention-hilt")
  id("convention-style")
  alias(libs.plugins.dependencyGuard)
}

val gitHash = gitVersionName()
val semVer = stringPropertyOrThrow(key = "actual.app.versionName")

dependencyGuard {
  configuration("debugCompileClasspath")
  configuration("debugRuntimeClasspath")
  configuration("releaseCompileClasspath")
  configuration("releaseRuntimeClasspath")
}

android {
  namespace = "dev.jonpoulton.actual.app"

  defaultConfig {
    applicationId = "dev.jonpoulton.actual.app"
    targetSdk = intPropertyOrThrow(key = "actual.android.targetSdk")
    versionCode = (System.currentTimeMillis() / 1000L).toInt()
    versionName = "$semVer ($gitHash)"
    buildConfigField("String", "GIT_HASH", "\"$gitHash\"")
    buildConfigField(
      "kotlinx.datetime.Instant",
      "BUILD_TIME",
      "kotlinx.datetime.Instant.Companion.fromEpochMilliseconds(${System.currentTimeMillis()}L)",
    )
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  packaging {
    resources.excludes.addAll(
      listOf(
        "META-INF/DEPENDENCIES",
        "META-INF/INDEX.LIST",
      ),
    )

    jniLibs {
      useLegacyPackaging = true
    }
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(projects.modules.core.di)
  implementation(projects.modules.core.res)
  implementation(projects.modules.core.ui)
  implementation(libs.alakazam.android.compose)
  implementation(libs.alakazam.android.core)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.compose.animation.core)
  implementation(libs.androidx.compose.foundation.core)
  implementation(libs.androidx.compose.foundation.layout)
  implementation(libs.androidx.compose.hilt)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.navigation)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.ui.text)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.savedstate)
  implementation(libs.androidx.navigation.commonKtx)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.dagger.core)
  implementation(libs.hilt.android)
  implementation(libs.javax.inject)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.datetime)
  implementation(libs.timber)
}
