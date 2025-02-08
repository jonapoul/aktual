package actual.about.ui

import actual.core.colorscheme.ColorSchemeType
import actual.core.config.BuildConfig
import actual.core.ui.ActualTheme
import actual.test.ActualTestActivity
import actual.test.TestBuildConfig
import actual.test.runTest
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import github.api.client.GithubApi
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AboutScreenTest {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1)
  val composeRule = createAndroidComposeRule<ActualTestActivity>()

  @BindValue
  lateinit var buildConfig: BuildConfig

  @BindValue
  lateinit var githubApi: GithubApi

  @Before
  fun before() {
    buildConfig = TestBuildConfig
    githubApi = TestGithubApi()

    hiltRule.inject()
    Intents.init()
  }

  @After
  fun after() {
    Intents.release()
  }

  @Test
  fun clickingReportIssuesLaunchesBrowser() = composeRule.runTest {
    // Given
    setContent()

    // When
    onNodeWithTag(Tags.ReportButton).performClick()

    // Then
    val expectedIntent = Matchers.allOf(
      hasAction(Intent.ACTION_VIEW),
      hasData(Uri.parse("${TestBuildConfig.repoUrl}/issues/new")),
    )
    Intents.intended(expectedIntent)
  }

  private fun ComposeContentTestRule.setContent() {
    setContent {
      val navController = TestNavHostController(LocalContext.current)
      navController.navigatorProvider.addNavigator(ComposeNavigator())
      ActualTheme(ColorSchemeType.Light) { AboutScreen(navController) }
    }
  }
}
