rootProject.name = "compiler-plugin"

apply(from = "../gradle/repositories.gradle.kts")

dependencyResolutionManagement {
  versionCatalogs {
    register("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
