@file:Suppress("UnstableApiUsage")

import aktual.gradle.ConventionLicensee.Companion.LICENSEE_REPORT_ASSET_NAME
import blueprint.core.gitVersionCode
import io.github.kdroidfilter.nucleus.desktop.application.dsl.CompressionLevel
import io.github.kdroidfilter.nucleus.desktop.application.dsl.TargetFormat
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

plugins {
  id("aktual.module.jvm")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
  id("aktual.convention.compose")
  alias(libs.plugins.nucleus)
}

// Same as gitVersionDate in blueprint, but with the year stripped from 2026 -> 26
val gitVersionDate =
  providers.gitVersionCode().map { seconds ->
    val date = Instant.ofEpochSecond(seconds.toLong()).atZone(ZoneOffset.UTC).toLocalDate()
    "%02d.%02d.%02d".format(Locale.ROOT, date.year % 100, date.monthValue, date.dayOfMonth)
  }

nucleus {
  application {
    mainClass = "aktual.app.desktop.MainKt"

    jvmArgs +=
      listOf(
        // Make sure java.sql.DriverManager is included
        "--add-modules",
        "java.sql",
      )

    buildTypes.release.proguard {
      obfuscate = true
      optimize = true
      configurationFiles.from(file("proguard-rules.pro"), file("../common-rules.pro"))
    }

    nativeDistributions {
      targetFormats(
        // windows
        TargetFormat.AppX,
        TargetFormat.Msi,
        TargetFormat.Nsis,
        TargetFormat.Portable,

        // mac
        TargetFormat.Dmg,
        TargetFormat.Pkg,

        // linux
        TargetFormat.AppImage,
        TargetFormat.Deb,
        TargetFormat.Flatpak,
        TargetFormat.Rpm,
        TargetFormat.Snap,
      )

      // Package metadata
      packageName = "Aktual Desktop"
      packageVersion = gitVersionDate.get()
      description = "Desktop app for the Actual budgeting software"
      homepage = "https://github.com/jonapoul/aktual"
      licenseFile = rootProject.layout.projectDirectory.file("LICENSE")

      // JDK modules
      modules("java.sql")

      // Nucleus features
      cleanupNativeLibs = true
      // enableAotCache = true // requires JDK25
      // splashImage = "splash.png"
      compressionLevel = CompressionLevel.Maximum
      artifactName = $$"${name}-${version}-${os}-${arch}.${ext}"

      val icon =
        rootProject.isolated.projectDirectory.file(
          "aktual-core/l10n/src/commonMain/composeResources/drawable/app_icon_192.png"
        )

      windows {
        menu = true
        // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
        upgradeUuid = "a61b72be-1b0c-4de5-9607-791c17687428"
        iconFile = icon
        nsis {
          oneClick = false
          allowToChangeInstallationDirectory = true
          createDesktopShortcut = true
          createStartMenuShortcut = true
        }
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

  nativeApplication {
    // TBC
  }
}

val copyLicenseeReportToResources by
  tasks.registering(Copy::class) {
    from(tasks.licensee.map { it.jsonOutput })
    into("src/main/resources")
    rename { LICENSEE_REPORT_ASSET_NAME }
  }

tasks.processResources.configure { dependsOn(copyLicenseeReportToResources) }

// Can't remove this afterEvaluate because apparently the compose plugin doesn't create the task in
// a normal way
afterEvaluate {
  tasks.named("proguardReleaseJars").configure {
    // Proguard won't create the path for us...
    val outputsDir = layout.buildDirectory.dir("outputs")
    doFirst { outputsDir.get().asFile.mkdirs() }
  }
}

dependencies {
  implementation(project(":aktual-app:di"))
  implementation(project(":aktual-app:nav:ui"))
  implementation(project(":aktual-core:logging:impl"))
  implementation(project(":aktual-prefs"))
  implementation(compose.desktop.currentOs)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.compose.material3)
  implementation(libs.compose.viewmodel)
  implementation(libs.kotlinx.coroutines.swing)
  implementation(libs.metrox.viewmodel.compose)

  nucleus {}
}
