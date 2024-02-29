import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  id("io.gitlab.arturbosch.detekt")
}

val libs = the<LibrariesForLibs>()

detekt {
  toolVersion = libs.versions.detekt.get()
  config.setFrom(files("${rootProject.rootDir}/detekt.yml"))
  buildUponDefaultConfig = true
}

tasks.withType<Detekt> {
  reports.html.required.set(true)
}

val detektMain = tasks.findByName("detektMain")
if (detektMain != null) {
  val check by tasks
  check.dependsOn(detektMain)
}
