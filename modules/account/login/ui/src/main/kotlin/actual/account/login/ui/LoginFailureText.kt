package actual.account.login.ui

import actual.account.login.res.LoginStrings
import actual.account.login.vm.LoginResult
import actual.core.ui.ActualFontFamily
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

@Stable
@Composable
internal fun LoginFailureText(
  result: LoginResult.Failure,
  modifier: Modifier = Modifier,
) {
  val errorMessage = when (result) {
    is LoginResult.InvalidPassword -> LoginStrings.failurePassword
    is LoginResult.HttpFailure -> LoginStrings.failureHttp(result.code, result.message)
    is LoginResult.NetworkFailure -> LoginStrings.failureNetwork(result.reason)
    is LoginResult.OtherFailure -> LoginStrings.failureOther(result.reason)
  }

  val theme = LocalTheme.current

  Text(
    modifier = modifier,
    text = errorMessage,
    fontFamily = ActualFontFamily,
    color = theme.errorText,
    textAlign = TextAlign.Center,
  )
}

@Preview
@Composable
private fun PreviewInvalidPassword() = PreviewColumn {
  LoginFailureText(LoginResult.InvalidPassword)
}

@Preview
@Composable
private fun PreviewHttpFailure() = PreviewColumn {
  LoginFailureText(LoginResult.HttpFailure(code = 404, message = "Resource not found"))
}

@Preview
@Composable
private fun PreviewNetworkFailure() = PreviewColumn {
  LoginFailureText(LoginResult.NetworkFailure(reason = "Network problem"))
}

@Preview
@Composable
private fun PreviewOtherFailure() = PreviewColumn {
  LoginFailureText(LoginResult.OtherFailure("Something broke"))
}
