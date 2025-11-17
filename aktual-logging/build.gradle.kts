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
  }

  androidHostTestDependencies {
    implementation(libs.logback.classic)
  }

  jvmMainDependencies {
    api(libs.kotlinx.datetime)
    implementation(libs.logback.classic)
  }

  sourceSets {
    val commonMain by getting
    val androidMain by getting
    val jvmMain by getting

    val jvmSharedMain by creating {
      dependsOn(commonMain)
      dependencies { compileOnly(libs.logback.classic) }
    }

    androidMain.dependsOn(jvmSharedMain)
    jvmMain.dependsOn(jvmSharedMain)
  }
}

configurations.configureEach {
  // recommended in https://github.com/tony19/logback-android/blob/main/README.md#quick-start
  if (name.contains("test", ignoreCase = true)) {
    exclude(module = "logback-android")
  }
}
