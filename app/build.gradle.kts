plugins {
  kotlin("android")
  alias(libs.plugins.agp.app)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
  id("convention-compose")
  id("convention-desugaring")
  id("convention-style")
  id("convention-test")
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
    resValues = true
  }
}

dependencies {
  implementation(projects.modules.di)
  implementation(projects.modules.core.ui)
  implementation(libs.alakazam.android.compose)
  implementation(libs.alakazam.di.hilt)
  implementation(libs.androidx.compose.activity)
  implementation(libs.androidx.compose.hilt)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.navigation)
  debugImplementation(libs.androidx.compose.uiToolingPreview)
  implementation(libs.androidx.navigation.commonKtx)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
}
