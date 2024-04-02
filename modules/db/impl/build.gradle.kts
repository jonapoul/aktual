plugins {
  id("module-android")
  alias(libs.plugins.sqldelight)
}

android {
  namespace = "dev.jonpoulton.actual.db"
}

sqldelight {
  databases {
    create("ActualDatabase") {
      packageName.set("dev.jonpoulton.actual.db")
    }
  }
}

dependencies {
  implementation(libs.sqldelight.android)
  implementation(libs.sqldelight.coroutines)
}
