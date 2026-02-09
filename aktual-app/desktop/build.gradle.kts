@file:Suppress("UnstableApiUsage")

import aktual.gradle.ConventionLicensee.Companion.LICENSEE_REPORT_ASSET_NAME
import blueprint.core.gitVersionDate
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  id("aktual.module.jvm")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.shadow)
  id("aktual.convention.compose")
}

val gitVersionDate = providers.gitVersionDate()

compose.desktop {
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
      configurationFiles.from(
          file("proguard-rules.pro"),
          file("../common-rules.pro"),
      )
    }

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Aktual Desktop"
      packageVersion = gitVersionDate.get()
      packageVersion = "1.0.0"

      modules("java.sql")

      val icon =
          rootProject.isolated.projectDirectory.file(
              "aktual-core:l10n/src/commonMain/composeResources/drawable/app_icon_192.png"
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
        shortcut = true
        packageName = "aktual.app.desktop"
        iconFile = icon
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

tasks.shadowJar {
  archiveClassifier.set("all")
  mergeServiceFiles()
  manifest { attributes["Main-Class"] = "aktual.app.desktop.MainKt" }
}

afterEvaluate {
  tasks.named("proguardReleaseJars") {
    // Proguard won't create the path for us...
    val outputsDir = layout.buildDirectory.dir("outputs")
    doFirst { outputsDir.get().asFile.mkdirs() }
  }
}

dependencies {
  implementation(project(":aktual-app:di"))
  implementation(project(":aktual-app:nav"))
  implementation(project(":aktual-core:prefs"))
  implementation(compose.desktop.currentOs)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.jetbrains.material3)
  implementation(libs.jetbrains.viewmodel)
  implementation(libs.kotlinx.coroutines.swing)
  implementation(libs.metrox.viewmodel.compose)
}
