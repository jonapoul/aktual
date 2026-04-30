package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.FailureCard
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.RounderCardShape
import aktual.core.ui.ThemeParameters
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun OpenIdLogin(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) {
  FailureCard(
    modifier = modifier.border(Dp.Hairline, theme.pillBorderDark, RounderCardShape).padding(8.dp),
    title = Strings.loginOpenidUnsupported,
    reason = null,
    action = null,
    background = theme.pillBorderDark,
  )
}

@Preview
@Composable
private fun PreviewOpenIdLogin(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) { OpenIdLogin() }
