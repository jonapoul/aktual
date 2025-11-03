plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
  }

  sourceSets {
    val jvmMain by getting
    val androidMain by getting
    val commonMain by getting

    register("jvmAndroidMain") {
      dependsOn(commonMain)
      androidMain.dependsOn(this)
      jvmMain.dependsOn(this)
    }
  }
}
