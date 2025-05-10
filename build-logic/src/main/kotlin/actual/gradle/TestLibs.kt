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
      getLibrary("test.mockk.core"),
      getLibrary("test.okio"),
      getLibrary("test.turbine"),
    )
  }
