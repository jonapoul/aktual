package aktual.gradle

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.LicenseePlugin
import app.cash.licensee.SpdxId
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
        allow(SpdxId.Apache_20)
        allow(SpdxId.MIT)
        allow(SpdxId.BSD_2_Clause) // hamcrest
        allow(SpdxId.BSD_3_Clause) // androidx.datastore:datastore-preferences-external-protobuf
        allow(SpdxId.EPL_10) // junit

        unusedAction(IGNORE)
      }
    }

  companion object {
    const val LICENSEE_REPORT_ASSET_NAME = "licensee.json"
  }
}
