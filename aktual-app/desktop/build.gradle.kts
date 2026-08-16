@file:Suppress("UnstableApiUsage")

import aktual.gradle.ConventionLicensee.Companion.LICENSEE_REPORT_ASSET_NAME
import blueprint.core.gitVersionCode
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
  id("aktual.module.jvm")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
  id("aktual.convention.compose")
}

// Same as gitVersionDate in blueprint, but with the year stripped from 2026 -> 26
val gitVersionDate =
  gitVersionCode().map { seconds ->
    val date = Instant.ofEpochSecond(seconds.toLong()).atZone(ZoneOffset.UTC).toLocalDate()
    "%02d.%02d.%02d".format(Locale.ROOT, date.year % 100, date.monthValue, date.dayOfMonth)
  }

compose.desktop {
  application {
    mainClass = "aktual.app.desktop.MainKt"

    jvmArgs(
      // Make sure java.sql.DriverManager is included
      "--add-modules",
      "java.sql",
    )

    buildTypes.release.proguard {
      version = libs.proguard.map { it.version }
      obfuscate = true
      optimize = true
      configurationFiles.from(file("proguard-rules.pro"), file("../common-rules.pro"))
    }

    nativeDistributions {
      targetFormats(
        // windows
        TargetFormat.Exe,
        TargetFormat.Msi,

        // mac
        TargetFormat.Dmg,
        TargetFormat.Pkg,

        // linux
        TargetFormat.Deb,
        TargetFormat.Rpm,
      )

      // Package metadata
      packageName = "Aktual Desktop"
      packageVersion = gitVersionDate.get()
      description = "Desktop app for the Actual budgeting software"
      licenseFile = rootProject.isolated.projectDirectory.file("LICENSE")

      // JDK modules
      modules("java.sql")

      val icon =
        rootProject.isolated.projectDirectory.file(
          "aktual-core/l10n/src/commonMain/composeResources/drawable/app_icon_192.png"
        )

      windows {
        menu = true
        // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
        upgradeUuid = "a61b72be-1b0c-4de5-9607-791c17687428"
        iconFile = icon
      }

      macOS {
        bundleID = "aktual.app.desktop"
        iconFile = icon
      }

      linux {
        packageName = "aktual.app.desktop"
        iconFile = icon
        shortcut = true
        debMaintainer = "Jon Poulton <jpoulton@pm.me>"
        appCategory = "Utility"
      }
    }
  }
}

// jpackage's own output filenames don't include os/arch, so distributables for different
// platforms all end up with the same name. Rename them after packaging.
val os = OperatingSystem.current()
val artifactOs =
  when {
    os.isWindows -> "windows"
    os.isMacOsX -> "macos"
    else -> "linux"
  }

val artifactArch =
  when (val arch = System.getProperty("os.arch")) {
    "x86_64",
    "amd64" -> "x64"
    "aarch64",
    "arm64" -> "arm64"
    else -> arch ?: "unknown"
  }

tasks.withType<AbstractJPackageTask>().configureEach {
  doLast {
    val ext = targetFormat.fileExt
    val destDir = destinationDir.get().asFile
    val produced =
      destDir.listFiles { f -> f.extension.equals(ext, ignoreCase = true) }?.singleOrNull()
        ?: return@doLast
    val artifactName = "aktual-desktop-${gitVersionDate.get()}-$artifactOs-$artifactArch.$ext"
    produced.renameTo(destDir.resolve(artifactName))
  }
}

val copyLicenseeReportToResources =
  tasks.register("copyLicenseeReportToResources", Copy::class) {
    from(tasks.licensee.map { it.jsonOutput })
    into("src/main/resources")
    rename { LICENSEE_REPORT_ASSET_NAME }
  }

tasks.processResources.configure { dependsOn(copyLicenseeReportToResources) }

afterEvaluate {
  // Can't remove this afterEvaluate because apparently the compose plugin doesn't create the task
  // in a normal way
  tasks.named("proguardReleaseJars").configure {
    // Proguard won't create the path for us...
    val outputsDir = layout.buildDirectory.dir("outputs")
    doFirst { outputsDir.get().asFile.mkdirs() }
  }

  // DAGP is added from settings file, so it won't exist at configure time. So we have to do this in
  // afterEvaluate
  listOf("explodeCodeSourceMain", "abiAnalysisMain").forEach { name ->
    tasks.named(name).configure { dependsOn(copyLicenseeReportToResources) }
  }
}

dependencies {
  implementation(project(":aktual-app:di"))
  implementation(project(":aktual-app:nav"))
  implementation(project(":aktual-app:ui-app"))
  implementation(project(":aktual-app:ui-budget"))
  implementation(project(":aktual-core:l10n"))
  implementation(project(":aktual-di:graphs"))
  implementation(project(":aktual-prefs"))
  implementation(compose.desktop.currentOs)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.kotlinx.coroutines.swing)
  implementation(libs.metrox.viewmodel)
  implementation(libs.metrox.viewmodel.compose)
}
