package aktual.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun Project.javaVersion(): Provider<JavaVersion> = intProperty("aktual.javaVersion").map(JavaVersion::toVersion)

fun Project.jvmTarget(): Provider<JvmTarget> = stringProperty("aktual.javaVersion").map(JvmTarget::fromTarget)

fun Project.stringProperty(key: String): Provider<String> = providers.gradleProperty(key)

fun Project.intProperty(key: String): Provider<Int> = stringProperty(key).map(String::toInt)

fun Project.boolProperty(key: String): Provider<Boolean> = stringProperty(key).map(String::toBoolean)
