package aktual.gradle

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.LicenseePlugin
import app.cash.licensee.UnusedAction.IGNORE
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class ConventionLicensee : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.apply(LicenseePlugin::class)

      extensions.configure<LicenseeExtension> {
        allow("Apache-2.0")
        allow("MIT")
        allow("BSD-2-Clause") // hamcrest
        allow("BSD-3-Clause") // androidx.datastore:datastore-preferences-external-protobuf
        allow("EPL-1.0") // junit

        allowUrl("https://opensource.org/license/mit") // slf4j

        unusedAction(IGNORE)
      }
    }

  companion object {
    const val LICENSEE_REPORT_ASSET_NAME = "licensee.json"
  }
}
