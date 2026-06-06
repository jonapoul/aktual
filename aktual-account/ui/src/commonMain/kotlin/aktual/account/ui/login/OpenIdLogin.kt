package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.FailureCard
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RounderCardShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun OpenIdLogin(modifier: Modifier = Modifier) {
  FailureCard(
    modifier = modifier.border(Dp.Hairline, colors.pillBorderDark, RounderCardShape).padding(8.dp),
    title = Strings.loginOpenidUnsupported,
    reason = null,
    action = null,
    background = colors.pillBackgroundLight,
  )
}

@Preview
@Composable
private fun PreviewOpenIdLogin(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { OpenIdLogin() }
