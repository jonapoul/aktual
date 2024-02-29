@file:Suppress("UnstableApiUsage")

import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs += listOf(
      "-Xjvm-default=all-compatibility",
      "-opt-in=kotlin.RequiresOptIn",
      "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    )
  }
}

extensions.configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

val libs = the<LibrariesForLibs>()
val implementation by configurations

dependencies {
  implementation(libs.kotlin.stdlib)
}
