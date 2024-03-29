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
    versionName = stringPropertyOrThrow(key = "actual.app.versionName")
    buildConfigField("String", "GIT_HASH", "\"${gitVersionName()}\"")
    buildConfigField(
      "kotlinx.datetime.Instant",
      "BUILD_TIME",
      "kotlinx.datetime.Instant.Companion.fromEpochMilliseconds(${System.currentTimeMillis()}L)",
    )
  }

  kotlinOptions {
    jvmTarget = "19"
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

hilt {
  enableAggregatingTask = true
  enableExperimentalClasspathAggregation = true
}

dependencies {
  implementation(projects.modules.core.connection)
  implementation(projects.modules.core.res)
  implementation(projects.modules.core.ui)
  implementation(projects.modules.serverUrl.prefs)
  implementation(projects.modules.nav)
  implementation(libs.alakazam.android.core)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.savedstate)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.androidx.preference.ktx)
  implementation(libs.dagger.core)
  implementation(libs.flowpreferences)
  implementation(libs.hilt.android)
  implementation(libs.javaxInject)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.datetime)
}
