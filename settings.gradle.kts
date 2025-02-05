@file:Suppress("UnstableApiUsage", "HasPlatformType")

rootProject.name = "actual-android"

pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenLocal()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    mavenCentral()
    maven("https://jitpack.io")
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

private val File.gradleModuleDescendants: Sequence<File>
  get() = listFiles()
    ?.asSequence()
    ?.filter { it.isDirectory }
    ?.flatMap { dir -> dir.subDescendants }
    ?: emptySequence()

private val File.subDescendants: Sequence<File>
  get() = if (File(this, "build.gradle.kts").exists()) {
    sequenceOf(this)
  } else {
    gradleModuleDescendants
  }

val modulesDir = rootProject.projectDir.resolve("modules")
val modulesPath = modulesDir.toPath()

modulesDir.gradleModuleDescendants.forEach { dir ->
  val path = dir.toPath()
  val relative = modulesPath.relativize(path).toString().replace(File.separator, ":")
  include(relative)
  project(":$relative").projectDir = dir
}
