/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.account.ui.login

import actual.core.model.Password
import actual.test.assertEditableTextEquals
import actual.test.runTest
import actual.test.setAndroidThemedContent
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isFalse
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import actual.core.ui.Tags as CoreUiTags

@RunWith(AndroidJUnit4::class)
class PasswordLoginTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun `Can't click button if loading`() = composeRule.runTest {
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
  fun `Entered password is censored`() = composeRule.runTest {
    // given
    setAndroidThemedContent {
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
