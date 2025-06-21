package actual.account.ui.login

import actual.account.domain.LoginResult
import actual.core.ui.ActualFontFamily
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.l10n.Strings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

@Stable
@Composable
internal fun LoginFailureText(
  result: LoginResult.Failure,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val errorMessage = when (result) {
    is LoginResult.InvalidPassword -> Strings.loginFailurePassword
    is LoginResult.HttpFailure -> Strings.loginFailureHttp(result.code, result.message)
    is LoginResult.NetworkFailure -> Strings.loginFailureNetwork(result.reason)
    is LoginResult.OtherFailure -> Strings.loginFailureOther(result.reason)
  }

  Text(
    modifier = modifier.testTag(Tags.LoginFailureText),
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
