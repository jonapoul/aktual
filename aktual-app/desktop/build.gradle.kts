import aktual.gradle.ConventionLicensee.Companion.LICENSEE_REPORT_ASSET_NAME
import aktual.gradle.gitVersionName
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.module.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.shadow)
  alias(libs.plugins.convention.compose)
}

compose.desktop {
  application {
    mainClass = "aktual.app.desktop.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Aktual Desktop"
      packageVersion = gitVersionName().get()
      packageVersion = "1.0.0"

      windows {
        menu = true
        // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
        upgradeUuid = "a61b72be-1b0c-4de5-9607-791c17687428"
      }

      macOS {
        bundleID = "aktual.app.desktop"
      }

      linux {
        shortcut = true
        packageName = "aktual.app.desktop"
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
  implementation(project(":aktual-app:di"))
  implementation(project(":aktual-app:nav"))
  implementation(project(":aktual-prefs"))
  implementation(compose.desktop.currentOs)
  implementation(libs.jetbrains.material3)
  implementation(libs.jetbrains.viewmodel)
  implementation(libs.kotlinx.coroutines.swing)
  implementation(libs.metrox.viewmodel.compose)
}
