package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.model.LoginMethod
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.RounderCardShape
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun HeaderLogin(
  isLoading: Boolean,
  hasFailure: Boolean,
  onAction: (LoginAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val latestOnAction by rememberUpdatedState(onAction)
  LaunchedEffect(Unit) { latestOnAction(LoginAction.SignIn) }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .border(Dp.Hairline, theme.pillBorderDark, RounderCardShape)
        .background(theme.pillBackgroundLight, RounderCardShape)
        .padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    if (isLoading) {
      CircularProgressIndicator()

      Text(
        text = Strings.loginHeaderChecking,
        style = AktualTypography.bodyMedium,
        color = theme.tableRowHeaderText,
      )
    }

    if (hasFailure) {
      NormalTextButton(
        text = Strings.loginHeaderFallback,
        onClick = { onAction(LoginAction.SelectLoginMethod(LoginMethod.Password)) },
      )
    }
  }
}

@Preview
@Composable
private fun PreviewHeaderLogin(
  @PreviewParameter(HeaderLoginProvider::class) params: ThemedParams<HeaderLoginParams>
) =
  PreviewWithColorScheme(params.theme) {
    val data = params.data
    HeaderLogin(isLoading = data.isLoading, hasFailure = data.hasFailure, onAction = {})
  }

private data class HeaderLoginParams(
  val isLoading: Boolean = false,
  val hasFailure: Boolean = false,
)

private class HeaderLoginProvider :
  ThemedParameterProvider<HeaderLoginParams>(
    HeaderLoginParams(isLoading = false, hasFailure = false),
    HeaderLoginParams(isLoading = true, hasFailure = false),
    HeaderLoginParams(isLoading = false, hasFailure = true),
    HeaderLoginParams(isLoading = true, hasFailure = true),
  )
