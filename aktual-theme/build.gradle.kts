plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(project(":aktual-theme:model"))
    compileOnly(libs.compose.runtime)
    implementation(project(":aktual-logging"))
  }

  // UpstreamColorsTest reads the upstream theme tracker's resolved colours
  sourceSets.named("desktopTest") {
    resources.srcDir(isolated.rootProject.projectDirectory.dir(".github/upstream-theme-tracker"))
  }
}
