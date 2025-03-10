import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    implementation(libs.alakazam.kotlin.core)
  }
}
