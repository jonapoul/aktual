package aktual.gradle.dsl

import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun ExtensionAware.kotlin(action: KotlinMultiplatformExtension.() -> Unit) =
  extensions.configure(KotlinMultiplatformExtension::class, action)
