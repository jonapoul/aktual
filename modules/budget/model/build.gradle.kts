plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

tasks.withType<Test>().configureEach {
  systemProperty("test.resourcesDir", file("src/commonTest/resources").absolutePath)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:core:model"))
    api(libs.alakazam.kotlin.serialization)
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.immutable)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
    compileOnly(libs.alakazam.kotlin.composeAnnotations)
  }
}
