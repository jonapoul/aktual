plugins {
  alias(libs.plugins.kotlin.jvm)
}

group = "aktual.compiler"

version = "1.0.0"

val javaFile = layout.projectDirectory.file("../.java-version")
val jdkVersion = providers.fileContents(javaFile).asText.map { it.trim().toInt() }

kotlin {
  jvmToolchain(jdkVersion.get())
  compilerOptions {
    allWarningsAsErrors.set(true)
    freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
  }
}

dependencies {
  compileOnly(kotlin("compiler-embeddable"))
}
