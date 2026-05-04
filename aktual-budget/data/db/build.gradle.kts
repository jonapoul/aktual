import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.sqldelight)
}

sqldelight {
  databases {
    register("BudgetDatabase") {
      packageName = "aktual.budget.db"
      schemaOutputDirectory = file("src/commonMain/sqldelight/schemas")
      verifyMigrations = true
      verifyDefinitions = true
      generateAsync = true
      dialect(libs.sqldelight.dialect)
      module(libs.sqldelight.json)
    }
  }
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(libs.androidx.sqlite)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.driver.androidx)
    implementation(libs.sqldelight.primitive)
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.driver.sqlite)
    implementation(project(":aktual-test"))
  }
}

// The Android NativeLibraryLoader calls System.loadLibrary, which needs libsqliteJni.so on
// java.library.path. AGP adds src/androidHostTest/jniLibs to that path for host tests. We
// extract the JVM .so (glibc-linked, compatible with the host JVM) from sqlite-bundled-jvm.
val sqliteJniNative =
  configurations.register("sqliteJniNative") {
    isTransitive = false
    isCanBeConsumed = false
  }

dependencies { sqliteJniNative(libs.androidx.sqliteBundledJvm) }

val extractSqliteJniForHostTests =
  tasks.register<Copy>("extractSqliteJniForHostTests") {
    group = "Sqldelight"
    val osName = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    val nativesDir =
      when {
        "linux" in osName && ("aarch64" in arch || "arm64" in arch) -> "natives/linux_arm64"
        "linux" in osName -> "natives/linux_x64"
        "mac" in osName && ("aarch64" in arch || "arm64" in arch) -> "natives/osx_arm64"
        "mac" in osName -> "natives/osx_x64"
        "win" in osName -> "natives/windows_x64"
        else -> "natives/linux_x64"
      }
    from(zipTree(sqliteJniNative.map { it.singleFile })) {
      include("$nativesDir/*")
      eachFile { relativePath = RelativePath(true, name) }
      includeEmptyDirs = false
    }
    into(layout.projectDirectory.dir("src/androidHostTest/jniLibs"))
  }

tasks.withType<Test>().configureEach { dependsOn(extractSqliteJniForHostTests) }
