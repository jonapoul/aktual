@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "19"
    freeCompilerArgs += listOf(
      "-Xjvm-default=all-compatibility",
      "-opt-in=kotlin.RequiresOptIn",
    )
  }
}

extensions.configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_19
  targetCompatibility = JavaVersion.VERSION_19
}
