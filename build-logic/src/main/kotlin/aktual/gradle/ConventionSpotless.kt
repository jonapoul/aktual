package aktual.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class ConventionSpotless : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(SpotlessPlugin::class)
    }

    extensions.configure<SpotlessExtension> {
      format("licenseKotlin") {
        licenseHeaderFile(rootProject.file("config/spotless.kt"), "(package|@file:)")
        target("src/*/kotlin/**/*.kt")
      }
    }
  }
}
