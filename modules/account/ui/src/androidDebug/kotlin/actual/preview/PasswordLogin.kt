package actual.preview

import actual.core.model.Password
import actual.account.ui.login.PasswordLogin
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun Loading() = PreviewThemedColumn {
  PasswordLogin(
    isLoading = true,
    enteredPassword = Password.Dummy,
    onAction = {},
  )
}

@Preview
@Composable
private fun Filled() = PreviewThemedColumn {
  PasswordLogin(
    isLoading = false,
    enteredPassword = Password.Dummy,
    onAction = {},
  )
}

@Preview
@Composable
private fun Empty() = PreviewThemedColumn {
  PasswordLogin(
    isLoading = false,
    enteredPassword = Password.Empty,
    onAction = {},
  )
}
