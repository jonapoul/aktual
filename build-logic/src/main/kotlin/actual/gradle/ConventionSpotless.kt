package actual.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class ConventionSpotless : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(plugins) {
      apply("com.diffplug.spotless")
    }

    extensions.getByType(SpotlessExtension::class).apply {
      format("misc") {
        target("*.gradle", "*.gitignore", "*.pro")
        leadingTabsToSpaces()
        trimTrailingWhitespace()
        endWithNewline()
      }
      json {
        target("*.json")
        simple()
      }
      yaml {
        target("*.yml", "*.yaml")
        jackson()
      }
      format("xml") {
        target("*.xml")
        eclipseWtp(EclipseWtpFormatterStep.XML)
      }
    }
  }
}
