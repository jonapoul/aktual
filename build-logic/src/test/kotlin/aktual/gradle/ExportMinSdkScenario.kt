package aktual.gradle

import assertk.assertThat
import blueprint.test.Scenario
import blueprint.test.ScenarioTest
import blueprint.test.assertThatTask
import blueprint.test.buildGradleKts
import blueprint.test.buildsSuccessfully
import blueprint.test.contentEquals
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.settingsGradleKts
import blueprint.test.taskFailed
import blueprint.test.taskSucceeded
import blueprint.test.withConfigurationCache
import blueprint.test.withGradleProperty
import kotlin.test.Test
import org.gradle.util.GradleVersion

class ExportMinSdkScenario : ScenarioTest() {
  override val gradleVersion: String = GradleVersion.current().version

  override val fileTree = fileTree {
    settingsGradleKts()

    buildGradleKts(
      """
      import aktual.gradle.ExportMinSdkTask

      plugins {
        id("aktual.convention.idea") apply false
      }

      val readme = layout.projectDirectory.file("README.md")

      tasks.register<ExportMinSdkTask>("exportMinSdk") {
        minSdk = providers.gradleProperty("minSdk").map { it.toInt() }
        readmeFile = readme
        outputFile = readme
      }
      """
        .trimIndent()
    )

    "README.md"(readme(minSdk = 28))
  }

  @Test
  fun `a matching min SDK leaves the readme alone`() = runScenario {
    assertThatExportMinSdk(minSdk = 28).buildsSuccessfully().taskSucceeded(":exportMinSdk")
    assertThat(rootDir.resolve("README.md")).contentEquals(readme(minSdk = 28))
  }

  @Test
  fun `a different min SDK updates the readme and fails`() = runScenario {
    assertThatExportMinSdk(minSdk = 31)
      .failsBuild()
      .taskFailed(":exportMinSdk")
      .outputContains("with minSdk=31 - you need to commit it!")
    assertThat(rootDir.resolve("README.md")).contentEquals(readme(minSdk = 31))
  }

  @Test
  fun `running again after an update passes`() = runScenario {
    assertThatExportMinSdk(minSdk = 31).failsBuild()
    assertThatExportMinSdk(minSdk = 31).buildsSuccessfully().taskSucceeded(":exportMinSdk")
  }

  private fun Scenario.assertThatExportMinSdk(minSdk: Int) =
    assertThatTask(":exportMinSdk")
      .withConfigurationCache()
      .withGradleProperty(name = "minSdk", value = minSdk)

  private fun readme(minSdk: Int) =
    """
    # Project

    <a href="https://android-arsenal.com/api?level=$minSdk"><img alt="API" src="https://img.shields.io/badge/API-$minSdk%2B-brightgreen.svg?style=flat"/></a>

    Other text mentioning API 99 and level 12
    """
      .trimIndent()
}
