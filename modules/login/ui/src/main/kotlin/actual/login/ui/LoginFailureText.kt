package actual.login.ui

import actual.core.res.CoreStrings
import actual.core.ui.ActualFontFamily
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewActualColumn
import actual.login.vm.LoginResult
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
    is LoginResult.InvalidPassword -> CoreStrings.loginFailurePassword
    is LoginResult.HttpFailure -> CoreStrings.loginFailureHttp(result.code, result.message)
    is LoginResult.NetworkFailure -> CoreStrings.loginFailureNetwork(result.reason)
    is LoginResult.OtherFailure -> CoreStrings.loginFailureOther(result.reason)
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
private fun PreviewInvalidPassword() = PreviewActualColumn {
  LoginFailureText(LoginResult.InvalidPassword)
}

@Preview
@Composable
private fun PreviewHttpFailure() = PreviewActualColumn {
  LoginFailureText(LoginResult.HttpFailure(code = 404, message = "Resource not found"))
}

@Preview
@Composable
private fun PreviewNetworkFailure() = PreviewActualColumn {
  LoginFailureText(LoginResult.NetworkFailure(reason = "Network problem"))
}

@Preview
@Composable
private fun PreviewOtherFailure() = PreviewActualColumn {
  LoginFailureText(LoginResult.OtherFailure("Something broke"))
}
