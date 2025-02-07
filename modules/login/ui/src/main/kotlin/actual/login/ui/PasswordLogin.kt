package actual.login.ui

import actual.core.ui.ActualTextField
import actual.core.ui.PreviewActualColumn
import actual.core.ui.PrimaryActualTextButtonWithLoading
import actual.core.ui.VerticalSpacer
import actual.login.model.Password
import actual.login.res.LoginStrings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun PasswordLogin(
  isLoading: Boolean,
  enteredPassword: Password,
  onAction: (LoginAction) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
  ) {
    ActualTextField(
      modifier = Modifier.fillMaxWidth(1f),
      value = enteredPassword.toString(),
      onValueChange = { password -> onAction(LoginAction.EnterPassword(password)) },
      placeholderText = LoginStrings.passwordHint,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Go,
      ),
      keyboardActions = KeyboardActions(
        onGo = { onAction(LoginAction.SignIn) },
      ),
    )

    VerticalSpacer(20.dp)

    PrimaryActualTextButtonWithLoading(
      modifier = Modifier
        .align(Alignment.End)
        .width(170.dp),
      text = LoginStrings.signIn,
      isLoading = isLoading,
      onClick = { onAction(LoginAction.SignIn) },
    )
  }
}

@Preview
@Composable
private fun Loading() = PreviewActualColumn {
  PasswordLogin(
    isLoading = true,
    enteredPassword = Password.Dummy,
    onAction = {},
  )
}

@Preview
@Composable
private fun Filled() = PreviewActualColumn {
  PasswordLogin(
    isLoading = false,
    enteredPassword = Password.Dummy,
    onAction = {},
  )
}

@Preview
@Composable
private fun Empty() = PreviewActualColumn {
  PasswordLogin(
    isLoading = false,
    enteredPassword = Password.Empty,
    onAction = {},
  )
}
