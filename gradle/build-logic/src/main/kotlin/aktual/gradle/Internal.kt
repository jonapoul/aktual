package aktual.gradle

import blueprint.core.get
import blueprint.core.libs
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun ExtensionAware.kotlin(action: Action<KotlinMultiplatformExtension>) =
  extensions.configure(KotlinMultiplatformExtension::class, action)

internal val ExtensionAware.compose get() = extensions.getByType<ComposePlugin.Dependencies>()

internal val Project.testLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() = with(libs) {
    listOf(
      get("test.alakazam.core"),
      get("test.assertk"),
      get("test.kotlin.common"),
      get("test.kotlinx.coroutines"),
      get("test.ktor"),
      get("test.mockk"),
      get("test.turbine"),
    )
  }

internal val Project.androidTestLibraries: List<Provider<MinimalExternalModuleDependency>>
  get() = with(libs) {
    listOf(
      get("test.androidx.arch"),
      get("test.androidx.coreKtx"),
      get("test.androidx.junit"),
      get("test.androidx.rules"),
      get("test.androidx.runner"),
      get("test.robolectric"),
    )
  }

internal fun PluginContainer.withAnyId(vararg ids: String, action: Action<in Plugin<*>>) {
  ids.forEach { withId(it, action) }
}
