package org.gradle.kotlin.dsl

import blueprint.core.multiplatformDependencies as blueprintMultiplatformDependencies
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension as KMPExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KMPExtension.multiplatformDependencies(
  name: String,
  handler: KotlinDependencyHandler.() -> Unit,
): Unit = blueprintMultiplatformDependencies(name, handler)

fun KMPExtension.commonMainDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "commonMain", handler)

fun KMPExtension.commonTestDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "commonTest", handler)

fun KMPExtension.androidMainDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "androidMain", handler)

fun KMPExtension.androidHostTestDependencies(handler: KotlinDependencyHandler.() -> Unit) =
  blueprintMultiplatformDependencies("androidHostTest", handler)

fun KMPExtension.desktopMainDependencies(handler: KotlinDependencyHandler.() -> Unit) =
  blueprintMultiplatformDependencies("desktopMain", handler)

fun KMPExtension.desktopTestDependencies(handler: KotlinDependencyHandler.() -> Unit) =
  blueprintMultiplatformDependencies("desktopTest", handler)

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
