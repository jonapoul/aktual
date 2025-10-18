import actual.gradle.LICENSEE_REPORT_ASSET_NAME
import actual.gradle.versionName
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.module.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.convention.compose)
}

compose.desktop {
  application {
    mainClass = "actual.app.desktop.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Actual Desktop"
      packageVersion = versionName()
      packageVersion = "1.0.0"

      windows {
        menu = true
        // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
        upgradeUuid = "a61b72be-1b0c-4de5-9607-791c17687428"
      }

      macOS {
        bundleID = "actual.app.desktop"
      }

      linux {
        shortcut = true
        packageName = "actual.app.desktop"
      }
    }
  }

  nativeApplication {
    // TBC
  }
}

val copyLicenseeReportToResources by tasks.registering(Copy::class) {
  from(tasks.licensee.map { it.jsonOutput })
  into("src/main/resources")
  rename { LICENSEE_REPORT_ASSET_NAME }
}

tasks.processResources.configure {
  dependsOn(copyLicenseeReportToResources)
}

dependencies {
  implementation(project(":app:di"))
  implementation(project(":app:nav"))
  implementation(project(":modules:prefs"))
  implementation(compose.desktop.currentOs)
  implementation(compose.material3)
  implementation(libs.jetbrains.lifecycle.viewmodel.compose)
  implementation(libs.kotlinx.coroutines.swing)
}
