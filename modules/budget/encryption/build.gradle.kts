@file:Suppress("unused")

import blueprint.core.commonMainDependencies
import blueprint.core.jvmTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.okio)
    api(projects.modules.account.model)
    api(projects.modules.budget.model)
    api(projects.modules.core.model)
  }

  jvmTestDependencies {
    implementation(projects.modules.test.files)
  }

  sourceSets {
    val jvmMain by getting
    val androidMain by getting
    val commonMain by getting

    val jvmAndroidMain by registering {
      dependsOn(commonMain)
      androidMain.dependsOn(this)
      jvmMain.dependsOn(this)
    }
  }
}
