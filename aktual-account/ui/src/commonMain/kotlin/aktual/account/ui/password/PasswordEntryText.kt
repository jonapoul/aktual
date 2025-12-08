package aktual.account.ui.password

import aktual.core.model.Password
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TextField
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun PasswordEntryText(
  password: Password,
  placeholderText: String,
  showPassword: Boolean,
  onValueChange: (Password) -> Unit,
  modifier: Modifier = Modifier,
  imeAction: ImeAction = ImeAction.Go,
  onGo: (() -> Unit)? = null,
) {
  TextField(
    modifier = modifier,
    value = password.value,
    onValueChange = { value -> onValueChange(Password(value)) },
    placeholderText = placeholderText,
    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(
      autoCorrectEnabled = false,
      capitalization = KeyboardCapitalization.None,
      keyboardType = KeyboardType.Password,
      imeAction = imeAction,
    ),
    keyboardActions = if (onGo == null) KeyboardActions.Default else KeyboardActions(onGo = { onGo() }),
  )
}

@Preview
@Composable
private fun PreviewPasswordEntryText(
  @PreviewParameter(PasswordEntryProvider::class) params: ThemedParams<PasswordEntryParams>,
) = PreviewWithColorScheme(params.type) {
  PasswordEntryText(
    password = params.data.password,
    placeholderText = "Password",
    showPassword = params.data.showPassword,
    onValueChange = {},
    onGo = {},
  )
}

private data class PasswordEntryParams(
  val password: Password,
  val showPassword: Boolean,
)

private class PasswordEntryProvider : ThemedParameterProvider<PasswordEntryParams>(
  PasswordEntryParams(password = Password.Empty, showPassword = false),
  PasswordEntryParams(password = Password.Dummy, showPassword = false),
  PasswordEntryParams(password = Password.Dummy, showPassword = true),
)
