import org.gradle.kotlin.dsl.implementation

plugins {
  alias(libs.plugins.module.di)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:api:github"))
    api(project(":modules:budget:model"))
    api(project(":modules:core:di"))
    api(project(":modules:core:model"))
    api(project(":modules:prefs"))
    api(libs.alakazam.kotlin.core)
    api(libs.alakazam.kotlin.time)
    api(libs.androidx.lifecycle.viewmodel.core)
    api(libs.kotlinx.coroutines)
    api(libs.okio)
    api(libs.preferences.core)
    implementation(project(":modules:api:builder"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
  }

  androidMainDependencies {
    implementation(libs.androidx.crypto)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.preferences.android)
  }
}
