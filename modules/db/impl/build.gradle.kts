import actual.gradle.commonMainDependencies
import actual.gradle.commonTestDependencies
import actual.gradle.jvmMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.androidx.room)
  alias(libs.plugins.ksp)
  alias(libs.plugins.buildconfig)
}

val schemaDir = projectDir.resolve("schemas")

room {
  generateKotlin = true
  schemaDirectory(schemaDir.absolutePath)
}

buildConfig {
  packageName("actual.db")
  sourceSets.getByName("test") {
    buildConfigField(name = "SCHEMAS_PATH", schemaDir)
  }
}

kotlin {
  targets.configureEach {
    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions {
          freeCompilerArgs.add("-Xexpect-actual-classes")
        }
      }
    }
  }
}

commonMainDependencies {
  api(libs.androidx.room.runtime)
  api(libs.javaxInject)
  implementation(libs.androidx.room.common)
  implementation(libs.kotlinx.datetime)
}

commonTestDependencies {
  implementation(libs.test.alakazam.core)
  implementation(libs.test.androidx.room)
  implementation(projects.test.coroutines)
}

jvmMainDependencies {
  implementation(libs.androidx.sqlite.bundled)
}

dependencies {
  listOf(
    "kspAndroid",
    "kspAndroidTest",
    "kspCommonMainMetadata",
    "kspJvm",
    "kspJvmTest",
  ).forEach { config -> add(config, libs.androidx.room.compiler) }
}
