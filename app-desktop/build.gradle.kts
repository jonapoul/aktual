import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.module.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.convention.composeMp)
  alias(libs.plugins.dependencyGuard)
}

dependencyGuard {
  configuration("runtimeClasspath")
}

compose.desktop {
  application {
    mainClass = "actual.app.desktop.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Actual Desktop"
      packageVersion = "1.0.0"

      windows {
        menu = true
        // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
        upgradeUuid = "a61b72be-1b0c-4de5-9607-791c17687428"
      }

      macOS {
        bundleID = "org.jetbrains.compose.demo.widgets"
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

dependencies {
  implementation(compose.desktop.currentOs)
}
