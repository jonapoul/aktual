package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.model.Token
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PasswordTransformation
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.keyboardFocusRequester
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun ApiKeyLogin(
  isLoading: Boolean,
  enteredApiKey: Token,
  onAction: OnAction<LoginAction>,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    val apiKeyTextState = rememberTextFieldState(initialText = enteredApiKey.value)

    LaunchedEffect(apiKeyTextState) {
      snapshotFlow { apiKeyTextState.text.toString() }
        .collect { apiKey -> onAction(EnterApiKey(apiKey)) }
    }

    Text(
      text = Strings.loginApiKeyMessage,
      color = colors.tableRowHeaderText,
      style = typography.bodyMedium,
    )

    VerticalSpacer(12.dp)

    AktualTextField(
      modifier =
        Modifier.testTag(Tags.ApiKeyLoginTextField)
          .fillMaxWidth(1f)
          .focusRequester(keyboardFocusRequester(keyboard)),
      state = apiKeyTextState,
      isEnabled = !isLoading,
      placeholderText = Strings.loginApiKeyHint,
      outputTransformation = PasswordTransformation,
      keyboardOptions =
        KeyboardOptions(
          autoCorrectEnabled = false,
          capitalization = None,
          keyboardType = Password,
          imeAction = Go,
        ),
      onKeyboardAction = { _ ->
        keyboard?.hide()
        onAction(SignIn)
      },
    )

    VerticalSpacer(20.dp)

    AktualButton(
      kind = Primary,
      modifier = Modifier.testTag(Tags.ApiKeyLoginButton).padding(5.dp).fillMaxWidth(),
      text = Strings.loginSignIn,
      isLoading = isLoading,
      isEnabled = enteredApiKey.value.isNotEmpty(),
      onClick = { onAction(SignIn) },
    )
  }
}

@Preview
@Composable
private fun PreviewApiKeyLogin(
  @PreviewParameter(ApiKeyLoginProvider::class) params: ColorParams<ApiKeyLoginParams>,
) =
  PreviewWithColorParams(params) {
    ApiKeyLogin(isLoading = isLoading, enteredApiKey = apiKey, onAction = {})
  }

private data class ApiKeyLoginParams(val isLoading: Boolean, val apiKey: Token)

private class ApiKeyLoginProvider :
  ColorParamsProvider<ApiKeyLoginParams>(
    ApiKeyLoginParams(apiKey = Token("actual_abc123"), isLoading = true),
    ApiKeyLoginParams(apiKey = Token("actual_abc123"), isLoading = false),
    ApiKeyLoginParams(apiKey = Token(""), isLoading = false),
  )
