package actual.login.ui

import actual.core.ui.ActualFontFamily
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewActualColumn
import actual.login.vm.LoginResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import actual.core.res.R as CoreR

@Stable
@Composable
internal fun LoginFailureText(
  result: LoginResult.Failure,
  modifier: Modifier = Modifier,
) {
  val errorMessage = when (result) {
    is LoginResult.InvalidPassword -> stringResource(id = CoreR.string.login_failure_password)
    is LoginResult.HttpFailure -> stringResource(id = CoreR.string.login_failure_http, result.code, result.message)
    is LoginResult.NetworkFailure -> stringResource(id = CoreR.string.login_failure_network, result.reason)
    is LoginResult.OtherFailure -> stringResource(id = CoreR.string.login_failure_other, result.reason)
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
