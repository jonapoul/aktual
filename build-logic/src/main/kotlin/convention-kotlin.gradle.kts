@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "21"
    freeCompilerArgs += listOf(
      "-Xjvm-default=all-compatibility",
      "-opt-in=kotlin.RequiresOptIn",
    )
  }
}

extensions.configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}
