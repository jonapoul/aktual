import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.room)
  alias(libs.plugins.ksp)
}

room3 { schemaDirectory(layout.projectDirectory.dir("src/commonMain/schemas")) }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(libs.androidx.sqlite)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.runtime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(libs.room.testing)
    implementation(project(":aktual-test"))
  }
}

dependencies {
  "kspAndroid"(libs.room.compiler)
  "kspJvm"(libs.room.compiler)
}
