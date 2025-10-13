package actual.gradle

import blueprint.core.invoke
import blueprint.core.libs
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

internal val Project.testLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() = listOf(
    libs("test.alakazam.core"),
    libs("test.kotlin.common"),
    libs("test.kotlinx.coroutines"),
    libs("test.ktor"),
    libs("test.mockk"),
    libs("test.turbine"),
  )

internal val Project.androidTestLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() = listOf(
    libs("test.androidx.arch"),
    libs("test.androidx.coreKtx"),
    libs("test.androidx.junit"),
    libs("test.androidx.rules"),
    libs("test.androidx.runner"),
    libs("test.robolectric"),
  )
