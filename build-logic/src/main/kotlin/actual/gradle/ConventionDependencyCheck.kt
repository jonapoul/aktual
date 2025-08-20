@file:Suppress("MagicNumber")

package actual.gradle

import blueprint.core.rootLocalPropertiesOrNull
import blueprint.core.stringPropertyOrNull
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.owasp.dependencycheck.reporting.ReportGenerator.Format

class ConventionDependencyCheck : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    pluginManager.apply(DependencyCheckPlugin::class)

    extensions.configure<DependencyCheckExtension> {
      failBuildOnUnusedSuppressionRule = true
      failOnError = false
      formats = listOf(Format.HTML, Format.SARIF).map { it.toString() }
      outputDirectory = file("build/reports/dependency-check/").absolutePath
      scanBuildEnv = false
      scanDependencies = true
      showSummary = true
      suppressionFile = rootProject.file("config/dependency-check-suppressions.xml").absolutePath

      nvd {
        val key = "actual.nvdApiKey"
        apiKey = stringPropertyOrNull(key) ?: rootLocalPropertiesOrNull()?.get(key)?.toString()
        if (apiKey == null) {
          logger.warn("No NVD API key was provided! Check local.properties for $key")
        }
      }

      data { directory = gradle.gradleUserHomeDir.resolve("dependency-check-data").absolutePath }

      analyzers {
        archiveEnabled = false
        assemblyEnabled = false
        autoconfEnabled = false
        bundleAuditEnabled = false
        cmakeEnabled = false
        cocoapodsEnabled = false
        composerEnabled = false
        composerSkipDev = false
        cpanEnabled = false
        dartEnabled = false
        experimentalEnabled = false
        golangDepEnabled = false
        golangModEnabled = false
        msbuildEnabled = false
        nugetconfEnabled = false
        nuspecEnabled = false
        pyDistributionEnabled = false
        pyPackageEnabled = false
        rubygemsEnabled = false
        swiftEnabled = false
        swiftPackageResolvedEnabled = false

        nodeAudit { enabled = false }
        nodePackage { enabled = false }
        retirejs { enabled = false }
      }
    }
  }
}
