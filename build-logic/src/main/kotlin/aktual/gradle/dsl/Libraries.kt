package aktual.gradle.dsl

import blueprint.core.get
import blueprint.core.libs
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

val Project.testLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() =
    listOf(
        "alakazam.test",
        "assertk",
        "burst",
        "burstCoroutines",
        "kotlinx.coroutines.test",
        "mockk",
        "turbine",
      )
      .map(libs::get)

val Project.androidTestLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() =
    listOf(
        "androidx.test.arch",
        "androidx.test.core",
        "androidx.test.junit",
        "androidx.test.runner",
        "robolectric",
      )
      .map(libs::get)

val Project.composeLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() =
    listOf(
        "alakazam.compose",
        "androidx.lifecycle.runtime.compose",
        "androidx.lifecycle.viewmodel.compose",
        "compose.animation",
        "compose.foundation",
        "compose.material3",
        "compose.ui",
        "compose.uiToolingPreview",
        "compose.uiUtil",
        "kotlinx.immutable",
        "metrox.viewmodel.compose",
      )
      .map(libs::get)
