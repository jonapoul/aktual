plugins {
  id("module-android")
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

android {
  namespace = "dev.jonpoulton.actual.db"

  room {
    schemaDirectory("$projectDir/schemas")
  }
}

dependencies {
  api(projects.modules.db.api)
  api(libs.androidx.room.runtime)
  api(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)
}
