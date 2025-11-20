plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.logcat)
    api(libs.okio)
  }

  androidMainDependencies {
    implementation(libs.logback.android)
    implementation(libs.slf4j)
    compileOnly(libs.logback.classic)
  }

  androidHostTestDependencies {
    implementation(libs.logback.classic)
  }

  desktopMainDependencies {
    api(libs.kotlinx.datetime)
    implementation(libs.logback.classic)
    compileOnly(libs.logback.classic)
  }
}

configurations.configureEach {
  // recommended in https://github.com/tony19/logback-android/blob/main/README.md#quick-start
  if (name.contains("test", ignoreCase = true)) {
    exclude(module = "logback-android")
  }
}
