package aktual.gradle

import blueprint.core.get
import blueprint.core.libs
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun ExtensionAware.kotlin(action: Action<KotlinMultiplatformExtension>) =
  extensions.configure(KotlinMultiplatformExtension::class, action)

internal val Project.testLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() = listOf(
    "test.alakazam",
    "test.assertk",
    "test.kotlin.common",
    "test.kotlinx.coroutines",
    "test.ktor",
    "test.mockk",
    "test.turbine",
  ).map(libs::get)

internal val Project.androidTestLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() = listOf(
    "test.androidx.arch",
    "test.androidx.coreKtx",
    "test.androidx.junit",
    "test.androidx.rules",
    "test.androidx.runner",
    "test.robolectric",
  ).map(libs::get)
