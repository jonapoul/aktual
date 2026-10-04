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
import blueprint.test.outputDoesNotContain
import blueprint.test.settingsGradleKts
import blueprint.test.taskFailed
import blueprint.test.taskSucceeded
import blueprint.test.taskWasUpToDate
import blueprint.test.withConfigurationCache
import blueprint.test.withGradleProperty
import kotlin.test.Test
import org.gradle.util.GradleVersion
import org.intellij.lang.annotations.Language

class DetektReportScenario : ScenarioTest() {
  override val gradleVersion: String = GradleVersion.current().version

  override val fileTree = fileTree {
    settingsGradleKts()

    buildGradleKts(
      """
      import aktual.gradle.DetektReportTask

      plugins {
        id("aktual.convention.idea") apply false
      }

      tasks.register<DetektReportTask>("detektCheck") {
        checkstyleReports.from(
          providers.gradleProperty("reports").map { names -> names.split(",").map { it + ".xml" } }
        )
        basePath.set(layout.projectDirectory)
        reportFile.set(layout.buildDirectory.file("issues.txt"))
      }
      """
        .trimIndent()
    )

    "main.xml"(
      checkstyle(
        """
        <file name="src/main/kotlin/B.kt">
          <error line="12" column="3" severity="error" message="Too long" source="detekt.MaxLineLength" />
          <error line="2" column="10" severity="error" message="Magic &lt;number&gt;" source="detekt.MagicNumber" />
        </file>
        <file name="src/main/kotlin/A.kt">
          <error line="7" column="1" severity="warning" message="Unused" source="detekt.UnusedVariable" />
        </file>
        """
      )
    )

    "test.xml"(
      checkstyle(
        """
        <file name="src/main/kotlin/B.kt">
          <error line="12" column="3" severity="error" message="Too long" source="detekt.MaxLineLength" />
          <error line="12" column="3" severity="error" message="Empty" source="detekt.EmptyFunctionBlock" />
        </file>
        """
      )
    )

    "clean.xml"(checkstyle(""))
  }

  @Test
  fun `issues from all reports are deduplicated and sorted`() = runScenario {
    val root = rootDir.canonicalFile.invariantSeparatorsPath
    val expected =
      """
      file://$root/src/main/kotlin/A.kt:7:1 [UnusedVariable] Unused
      file://$root/src/main/kotlin/B.kt:2:10 [MagicNumber] Magic <number>
      file://$root/src/main/kotlin/B.kt:12:3 [EmptyFunctionBlock] Empty
      file://$root/src/main/kotlin/B.kt:12:3 [MaxLineLength] Too long
      """
        .trimIndent()

    assertThatDetektCheck("main,test,clean")
      .failsBuild()
      .taskFailed(":detektCheck")
      .outputContains(expected)
      .outputContains("> Found 4 detekt issue(s), see file://$root/build/issues.txt")

    assertThat(rootDir.resolve("build/issues.txt")).contentEquals(expected)
  }

  @Test
  fun `no issues passes with an empty report`() = runScenario {
    assertThatDetektCheck("clean").buildsSuccessfully().taskSucceeded(":detektCheck")
    assertThat(rootDir.resolve("build/issues.txt")).contentEquals("")
    assertThatDetektCheck("clean").buildsSuccessfully().taskWasUpToDate(":detektCheck")
  }

  @Test
  fun `a missing report is ignored`() = runScenario {
    assertThatDetektCheck("clean,missing")
      .buildsSuccessfully()
      .taskSucceeded(":detektCheck")
      .outputDoesNotContain("missing.xml")
  }

  @Test
  fun `issues are reported again on every run`() = runScenario {
    assertThatDetektCheck("main").failsBuild().outputContains("Found 3 detekt issue(s)")
    assertThatDetektCheck("main")
      .failsBuild()
      .outputContains("Reusing configuration cache.")
      .outputContains("Found 3 detekt issue(s)")
  }

  private fun Scenario.assertThatDetektCheck(reports: String) =
    assertThatTask(":detektCheck")
      .withConfigurationCache()
      .withGradleProperty(name = "reports", value = reports)

  private fun checkstyle(@Language("XML") files: String) =
    """
    |<?xml version="1.0" encoding="UTF-8"?>
    |<checkstyle version="4.3">
    |${files.trimIndent()}
    |</checkstyle>
    """
      .trimMargin()
}
