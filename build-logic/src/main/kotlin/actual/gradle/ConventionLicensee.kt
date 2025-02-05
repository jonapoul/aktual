package actual.gradle

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.LicenseePlugin
import app.cash.licensee.UnusedAction
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ConventionLicensee : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(LicenseePlugin::class.java)
    }

    extensions.configure<LicenseeExtension> {
      listOf(
        "Apache-2.0",
        "MIT",
        "EPL-1.0",
        "EPL-2.0",
        "BSD-2-Clause",
      ).forEach { spdxId ->
        allow(spdxId)
      }

      unusedAction(UnusedAction.IGNORE)
    }
  }
}
