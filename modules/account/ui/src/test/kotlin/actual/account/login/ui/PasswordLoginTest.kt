package actual.account.login.ui

import actual.account.model.Password
import actual.account.ui.login.LoginAction
import actual.account.ui.login.PasswordLogin
import actual.account.ui.login.Tags
import actual.test.assertEditableTextEquals
import actual.test.runTest
import actual.test.setThemedContent
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertFalse
import actual.core.ui.Tags as CoreUiTags

@RunWith(AndroidJUnit4::class)
class PasswordLoginTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun `Can't click button if loading`() = composeRule.runTest {
    // given
    var wasClicked = false
    setThemedContent {
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
    assertFalse(wasClicked)
  }

  @Test
  fun `Entered password is censored`() = composeRule.runTest {
    // given
    setThemedContent {
        PasswordLogin(
            isLoading = false,
            enteredPassword = PASSWORD,
            onAction = {},
        )
    }

    // then the text entry input is blocked out
    onNodeWithTag(Tags.PasswordLoginTextField)
      .assertEditableTextEquals("•••••••••")
  }

  private companion object {
    val PASSWORD = Password("P@ssw0rd!")
  }
}
