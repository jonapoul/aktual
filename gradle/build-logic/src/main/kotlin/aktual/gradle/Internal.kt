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
  get() =
    listOf("alakazam.test", "assertk", "kotlinx.coroutines.test", "mockk", "turbine").map(libs::get)

internal val Project.androidTestLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() =
    listOf(
        "androidx.test.arch",
        "androidx.test.coreKtx",
        "androidx.test.junit",
        "androidx.test.rules",
        "androidx.test.runner",
        "robolectric",
      )
      .map(libs::get)
