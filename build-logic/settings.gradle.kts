@file:Suppress("UnstableApiUsage")

rootProject.name = "build-logic"

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    gradlePluginPortal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenLocal()
  }

  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
