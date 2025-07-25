plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.okio)
    api(project(":modules:account:model"))
    api(project(":modules:budget:model"))
    api(project(":modules:core:model"))
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
