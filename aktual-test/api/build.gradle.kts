import org.gradle.internal.extensions.stdlib.capitalized

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.buildconfig)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-api:actual"))
    implementation(project(":aktual-test:kotlin"))
    implementation(kotlin("test"))
  }
}

val allowedExtensions = setOf("json", "txt")

buildConfig {
  packageName("aktual.test")
  className("Responses")
  useKotlinOutput { internalVisibility = false }

  rootProject
    .layout
    .projectDirectory
    .dir("api/actual")
    .asFileTree
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
