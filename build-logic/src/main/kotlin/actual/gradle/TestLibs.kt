package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

internal val Project.testLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() = with(libs) {
    listOf(
      getLibrary("test.alakazam.core"),
      getLibrary("test.kotlin.common"),
      getLibrary("test.kotlinx.coroutines"),
      getLibrary("test.ktor"),
      getLibrary("test.mockk"),
      getLibrary("test.turbine"),
    )
  }

internal val Project.androidTestLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() = with(libs) {
    listOf(
      getLibrary("test.androidx.arch"),
      getLibrary("test.androidx.coreKtx"),
      getLibrary("test.androidx.junit"),
      getLibrary("test.androidx.rules"),
      getLibrary("test.androidx.runner"),
      getLibrary("test.robolectric"),
    )
  }
