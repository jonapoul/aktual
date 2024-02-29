import org.gradle.accessors.dm.LibrariesForLibs
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
  id("org.jlleitschuh.gradle.ktlint")
}

val libs = the<LibrariesForLibs>()

ktlint {
  version.set(libs.versions.ktlint.cli.get())
  reporters {
    reporter(ReporterType.HTML)
  }
}
