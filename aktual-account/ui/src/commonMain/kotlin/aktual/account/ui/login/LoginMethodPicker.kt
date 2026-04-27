package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.model.LoginMethod
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.Dimens
import aktual.core.ui.RounderCardShape
import aktual.core.ui.SlidingToggleButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LoginMethodPicker(
  methods: ImmutableList<LoginMethod>,
  selectedMethod: LoginMethod,
  onAction: (LoginAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .border(Dp.Hairline, theme.pillBorderDark, RounderCardShape)
        .background(theme.pillBackgroundLight, RounderCardShape)
        .padding(Dimens.Large),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      modifier = Modifier.fillMaxWidth().padding(Dimens.VeryLarge),
      text = Strings.loginMethodSelect,
      color = theme.tableRowHeaderText,
      textAlign = TextAlign.Start,
      style = AktualTypography.bodyLarge,
      fontWeight = FontWeight.Bold,
    )

    SlidingToggleButton(
      modifier = Modifier.fillMaxWidth().padding(Dimens.VeryLarge),
      options = methods,
      selected = selectedMethod,
      onSelect = { m -> onAction(LoginAction.SelectLoginMethod(m)) },
      string = { m -> m.string() },
    )
  }
}

@Composable
private fun LoginMethod.string(): String =
  when (this) {
    LoginMethod.Password -> Strings.loginMethodPassword
    LoginMethod.Header -> Strings.loginMethodHeader
    LoginMethod.OpenId -> Strings.loginMethodOpenid
  }
