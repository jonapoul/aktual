package aktual.account.ui.login

import aktual.core.l10n.Strings
import aktual.core.model.LoginMethod
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTypography
import aktual.core.ui.Dimens
import aktual.core.ui.RounderCardShape
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
  onAction: LoginActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .border(Dp.Hairline, colors.pillBorderDark, RounderCardShape)
        .background(colors.pillBackgroundLight, RounderCardShape)
        .padding(Dimens.Large),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      modifier = Modifier.fillMaxWidth().padding(Dimens.VeryLarge),
      text = Strings.loginMethodSelect,
      color = colors.tableRowHeaderText,
      textAlign = TextAlign.Start,
      style = AktualTypography.bodyLarge,
      fontWeight = FontWeight.Bold,
    )

    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth().padding(Dimens.VeryLarge),
      options = methods,
      selected = selectedMethod,
      onSelect = { m -> onAction(SelectLoginMethod(m)) },
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
