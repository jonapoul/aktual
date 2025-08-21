package actual.preview

import actual.core.model.Password
import actual.account.ui.password.PasswordEntryText
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun EmptyInput() = PreviewThemedColumn {
  PasswordEntryText(
    password = Password(""),
    placeholderText = "Password",
    showPassword = false,
    onValueChange = {},
    onGo = {},
  )
}

@Preview
@Composable
private fun FullInput() = PreviewThemedColumn {
  PasswordEntryText(
    password = Password("abc-123"),
    placeholderText = "Password",
    showPassword = false,
    onValueChange = {},
    onGo = {},
  )
}

@Preview
@Composable
private fun ShowPassword() = PreviewThemedColumn {
  PasswordEntryText(
    password = Password("abc-123"),
    placeholderText = "Password",
    showPassword = true,
    onValueChange = {},
    onGo = {},
  )
}
