package aktual.gradle.dsl

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension as KMPExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

fun KMPExtension.androidHostTestDependencies(
  handler: KotlinDependencyHandler.() -> Unit
): NamedDomainObjectProvider<KotlinSourceSet> =
  sourceSets.named("androidHostTest") { ss -> ss.dependencies(handler) }

// Local replacements for blueprint's jvmMain/jvmTest helpers — the JVM target is named
// "desktop", so its source sets are desktopMain/desktopTest rather than jvmMain/jvmTest
fun KMPExtension.desktopMainDependencies(
  handler: KotlinDependencyHandler.() -> Unit
): NamedDomainObjectProvider<KotlinSourceSet> =
  sourceSets.named("desktopMain") { ss -> ss.dependencies(handler) }

fun KMPExtension.desktopTestDependencies(
  handler: KotlinDependencyHandler.() -> Unit
): NamedDomainObjectProvider<KotlinSourceSet> =
  sourceSets.named("desktopTest") { ss -> ss.dependencies(handler) }

internal fun Project.dependencies(configuration: DependencyHandler.() -> Unit) {
  dependencies.apply(configuration)
}

context(handler: DependencyHandler)
internal operator fun String.invoke(dependency: Any) = handler.add(this, dependency)

context(handler: DependencyHandler)
internal operator fun Configuration.invoke(dependency: Any) = handler.add(name, dependency)

context(handler: DependencyHandler)
internal operator fun NamedDomainObjectProvider<Configuration>.invoke(dependency: Any) =
  handler.add(name, dependency)
