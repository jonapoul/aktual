package aktual.gradle

import aktual.gradle.dsl.dependencies
import aktual.gradle.dsl.invoke
import aktual.gradle.dsl.withType
import blueprint.core.get
import blueprint.core.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test

/**
 * Apply this on modules that need to run androidHostTest test cases using real database instances
 */
class ConventionDatabaseTest : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      // The Android NativeLibraryLoader calls System.loadLibrary, which needs libsqliteJni.so on
      // java.library.path. AGP adds src/androidHostTest/jniLibs to that path for host tests. We
      // extract the JVM .so (glibc-linked, compatible with the host JVM) from sqlite-bundled-jvm.
      val sqliteJniNative =
        configurations.register("sqliteJniNative") { c ->
          c.isTransitive = false
          c.isCanBeConsumed = false
        }

      dependencies { sqliteJniNative(libs["androidx.sqliteBundledJvm"]) }

      val extractSqliteJniForHostTests =
        tasks.register("extractSqliteJniForHostTests", Copy::class.java) { t ->
          t.group = "Sqldelight"
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
          t.from(zipTree(sqliteJniNative.map { c -> c.singleFile })) { spec ->
            spec.include("$nativesDir/*")
            spec.eachFile { s -> s.relativePath = RelativePath(true, s.name) }
            spec.includeEmptyDirs = false
          }
          t.into(layout.projectDirectory.dir("src/androidHostTest/jniLibs"))
        }

      tasks.withType(Test::class).configureEach { t -> t.dependsOn(extractSqliteJniForHostTests) }
    }
}
