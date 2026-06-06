package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.model.Password
import aktual.core.ui.AktualTextField
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PasswordTransformation
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.PrimaryTextButtonWithLoading
import aktual.core.ui.keyboardFocusRequester
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun PasswordLogin(
  isLoading: Boolean,
  enteredPassword: Password,
  onAction: LoginActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    val passwordTextState = rememberTextFieldState(initialText = enteredPassword.value)

    LaunchedEffect(passwordTextState) {
      snapshotFlow { passwordTextState.text.toString() }
        .collect { password -> onAction(EnterPassword(password)) }
    }

    AktualTextField(
      modifier =
        Modifier.testTag(Tags.PasswordLoginTextField)
          .fillMaxWidth(1f)
          .focusRequester(keyboardFocusRequester(keyboard)),
      state = passwordTextState,
      isEnabled = !isLoading,
      placeholderText = Strings.loginPasswordHint,
      outputTransformation = PasswordTransformation,
      keyboardOptions =
        KeyboardOptions(
          autoCorrectEnabled = false,
          capitalization = KeyboardCapitalization.None,
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Go,
        ),
      onKeyboardAction = { _ ->
        keyboard?.hide()
        onAction(SignIn)
      },
    )

    VerticalSpacer(20.dp)

    PrimaryTextButtonWithLoading(
      modifier = Modifier.testTag(Tags.PasswordLoginButton).padding(5.dp).fillMaxWidth(),
      text = Strings.loginSignIn,
      isLoading = isLoading,
      isEnabled = enteredPassword != Password.Empty,
      onClick = { onAction(SignIn) },
    )
  }
}

@Preview
@Composable
private fun PreviewPasswordLogin(
  @PreviewParameter(PasswordLoginProvider::class) params: ColoredParams<PasswordLoginParams>
) =
  PreviewWithColors(params.colors) {
    PasswordLogin(isLoading = false, enteredPassword = Password.Dummy, onAction = {})
  }

private data class PasswordLoginParams(val isLoading: Boolean, val password: Password)

private class PasswordLoginProvider :
  ColoredParameterProvider<PasswordLoginParams>(
    PasswordLoginParams(password = Password.Dummy, isLoading = true),
    PasswordLoginParams(password = Password.Dummy, isLoading = false),
    PasswordLoginParams(password = Password.Empty, isLoading = false),
  )
