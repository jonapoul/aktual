package aktual.account.ui.login

import aktual.core.model.Password
import aktual.core.ui.Tags as CoreUiTags
import aktual.test.assertEditableTextEquals
import aktual.test.runTest
import aktual.test.setAndroidThemedContent
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isFalse
import kotlin.test.Test
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordLoginTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun `Can't click button if loading`() =
      composeRule.runTest {
        // given
        var wasClicked = false
        setAndroidThemedContent {
          PasswordLogin(
              isLoading = true,
              enteredPassword = PASSWORD,
              onAction = { wasClicked = it == LoginAction.SignIn },
          )
        }

        // then the sign in button is disabled
        onNodeWithTag(CoreUiTags.PrimaryTextButtonWithLoading).assertIsNotEnabled()

        // and the text input is disabled
        onNodeWithTag(Tags.PasswordLoginTextField).assertIsNotEnabled()

        // and clicking the button does nothing
        onNodeWithTag(Tags.PasswordLoginButton, useUnmergedTree = true).performClick()
        assertThat(wasClicked).isFalse()
      }

  @Test
  fun `Entered password is censored`() =
      composeRule.runTest {
        // given
        setAndroidThemedContent {
          PasswordLogin(
              isLoading = false,
              enteredPassword = PASSWORD,
              onAction = {},
          )
        }

        // then the text entry input is blocked out
        onNodeWithTag(Tags.PasswordLoginTextField).assertEditableTextEquals("•••••••••")
      }

  private companion object {
    val PASSWORD = Password("P@ssw0rd!")
  }
}
