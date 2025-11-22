import org.gradle.internal.extensions.stdlib.capitalized

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.redacted)
  alias(libs.plugins.buildconfig)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(libs.ktor.serialization.core)
    implementation(libs.ktor.serialization.json)
    implementation(project(":aktual-codegen:annotation"))
    compileOnly(libs.redacted.annotations)
  }
}

kspAllConfigs(project(":aktual-codegen:ksp"))

buildConfig {
  sourceSets.getByName("test") {
    packageName("aktual.test")
    className("Responses")
    useKotlinOutput()

    val allowedExtensions = setOf("json", "txt")
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
              val regularFile = layout.projectDirectory.file(file.absolutePath)
              val value = providers.fileContents(regularFile).asText.map { content -> "\"\"\"\n$content\"\"\"" }
              buildConfigField("String", name, value)
            }
        }
      }
  }
}
