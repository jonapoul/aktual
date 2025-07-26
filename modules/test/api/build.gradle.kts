import org.gradle.internal.extensions.stdlib.capitalized

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.buildconfig)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:api:actual"))
    implementation(project(":modules:test:kotlin"))
    implementation(kotlin("test"))
  }
}

val allowedExtensions = setOf("json", "txt")

buildConfig {
  packageName("actual.test")
  className("Responses")
  useKotlinOutput { internalVisibility = false }

  rootProject
    .layout
    .projectDirectory
    .dir("api/actual")
    .asFile
    .listFiles()
    .orEmpty()
    .filter { it.isDirectory }
    .forEach { dir ->
      forClass(dir.name.capitalized() + "Responses") {
        dir
          .listFiles()
          .orEmpty()
          .filter { it.extension.lowercase() in allowedExtensions }
          .forEach { file ->
            val name = file.nameWithoutExtension
              .replace("-", "_")
              .replace(".", "_")
              .uppercase()
            val value = "\"\"\"\n${file.readText()}\"\"\""
            buildConfigField("String", name, value)
          }
      }
    }
}
