@file:Suppress("DeprecatedCallableAddReplaceWith", "UnusedParameter", "UnusedReceiverParameter", "UNUSED_PARAMETER")

import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory.PluginFactory
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory.SubDependencyFactory
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope

internal const val ERROR_MESSAGE = "You probably meant to enter some child of the libs coordinate."

@Deprecated(message = ERROR_MESSAGE, level = DeprecationLevel.ERROR)
fun DependencyHandlerScope.implementation(factory: SubDependencyFactory): Unit = error("Not supported!")

@Deprecated(message = ERROR_MESSAGE, level = DeprecationLevel.ERROR)
fun DependencyHandlerScope.api(factory: SubDependencyFactory): Unit = error("Not supported!")

@Deprecated(message = ERROR_MESSAGE, level = DeprecationLevel.ERROR)
fun DependencyHandlerScope.compileOnly(factory: SubDependencyFactory): Unit = error("Not supported!")

@Deprecated(message = ERROR_MESSAGE, level = DeprecationLevel.ERROR)
fun DependencyHandlerScope.runtimeOnly(factory: SubDependencyFactory): Unit = error("Not supported!")

@Deprecated(message = ERROR_MESSAGE, level = DeprecationLevel.ERROR)
fun DependencyHandlerScope.testImplementation(factory: SubDependencyFactory): Unit = error("Not supported!")

@Deprecated(message = ERROR_MESSAGE, level = DeprecationLevel.ERROR)
fun PluginDependenciesSpecScope.alias(factory: PluginFactory): Unit = error("Not supported!")
