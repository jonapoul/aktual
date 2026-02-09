package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.model.Password
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.PrimaryTextButtonWithLoading
import aktual.core.ui.TextField
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.keyboardFocusRequester
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun PasswordLogin(
    isLoading: Boolean,
    enteredPassword: Password,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier,
    theme: Theme = LocalTheme.current,
) {
  Column(
      modifier = modifier,
  ) {
    val keyboard = LocalSoftwareKeyboardController.current

    TextField(
        modifier =
            Modifier.testTag(Tags.PasswordLoginTextField)
                .fillMaxWidth(1f)
                .focusRequester(keyboardFocusRequester(keyboard)),
        value = enteredPassword.value,
        enabled = !isLoading,
        onValueChange = { password -> onAction(LoginAction.EnterPassword(password)) },
        placeholderText = Strings.loginPasswordHint,
        visualTransformation = PasswordVisualTransformation(),
        theme = theme,
        keyboardOptions =
            KeyboardOptions(
                autoCorrectEnabled = false,
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go,
            ),
        keyboardActions =
            KeyboardActions(
                onGo = {
                  keyboard?.hide()
                  onAction(LoginAction.SignIn)
                },
            ),
    )

    VerticalSpacer(20.dp)

    PrimaryTextButtonWithLoading(
        modifier = Modifier.testTag(Tags.PasswordLoginButton).padding(5.dp).fillMaxWidth(),
        text = Strings.loginSignIn,
        isLoading = isLoading,
        onClick = { onAction(LoginAction.SignIn) },
    )
  }
}

@Preview
@Composable
private fun PreviewPasswordLogin(
    @PreviewParameter(PasswordLoginProvider::class) params: ThemedParams<PasswordLoginParams>,
) =
    PreviewWithColorScheme(params.type) {
      PasswordLogin(
          isLoading = false,
          enteredPassword = Password.Dummy,
          onAction = {},
      )
    }

private data class PasswordLoginParams(
    val isLoading: Boolean,
    val password: Password,
)

private class PasswordLoginProvider :
    ThemedParameterProvider<PasswordLoginParams>(
        PasswordLoginParams(password = Password.Dummy, isLoading = true),
        PasswordLoginParams(password = Password.Dummy, isLoading = false),
        PasswordLoginParams(password = Password.Empty, isLoading = false),
    )
