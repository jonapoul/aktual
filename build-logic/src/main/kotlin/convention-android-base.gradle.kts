@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.CommonExtension

extensions.getByType(CommonExtension::class).apply {
  compileSdk = intPropertyOrThrow(key = "actual.android.compileSdk")

  defaultConfig {
    minSdk = intPropertyOrThrow(key = "actual.android.minSdk")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
  }

  buildFeatures {
    // Enabled in modules that need them
    buildConfig = false
    compose = false
    resValues = false

    // Disable useless build steps
    aidl = false
    prefab = false
    renderScript = false
    shaders = false
    viewBinding = false
  }

  lint {
    abortOnError = true
    checkGeneratedSources = false
    checkReleaseBuilds = false
    checkTestSources = true
    explainIssues = true
    htmlReport = true
    xmlReport = true
    lintConfig = rootProject.file("lint.xml")
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      isReturnDefaultValues = true
    }
  }
}
