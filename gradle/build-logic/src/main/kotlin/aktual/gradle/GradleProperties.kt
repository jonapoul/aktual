package aktual.gradle

import blueprint.core.intProperty
import org.gradle.api.JavaVersion
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun ProviderFactory.javaVersion(): Provider<JavaVersion> = intProperty("aktual.javaVersion").map(JavaVersion::toVersion)

fun ProviderFactory.jvmTarget(): Provider<JvmTarget> = gradleProperty("aktual.javaVersion").map(JvmTarget::fromTarget)
