package aktual.app.nav

import aktual.core.l10n.Drawables
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.WithCompositionLocals
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppLockContent(
  viewModel: RootViewModel,
  onUnlock: (title: String, subtitle: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val title = Strings.appLockPromptTitle
  val subtitle = Strings.appLockPromptSubtitle

  // Saveable so a rotation doesn't show the prompt a second time
  var hasPrompted by rememberSaveable { mutableStateOf(false) }
  SideEffect {
    if (!hasPrompted) {
      hasPrompted = true
      onUnlock(title, subtitle)
    }
  }

  val isSystemDark = isSystemInDarkTheme()
  SideEffect(isSystemDark) { viewModel.updateSystemDarkTheme(isSystemDark) }
  val colors by viewModel.colors.collectAsStateWithLifecycle()

  WithCompositionLocals {
    AktualTheme(colors) { AppLockScreen(onUnlock = { onUnlock(title, subtitle) }, modifier) }
  }
}

@Composable
internal fun AppLockScreen(onUnlock: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize().background(colors.pageBackground).safeDrawingPadding(),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Image(
      modifier = Modifier.size(96.dp),
      painter = Drawables.appIcon192,
      contentDescription = null,
    )

    VerticalSpacer(20.dp)

    Text(
      text = Strings.appLockTitle,
      fontSize = 20.sp,
      fontWeight = W700,
      color = colors.pageText,
    )

    VerticalSpacer(20.dp)

    PrimaryTextButton(text = Strings.appLockUnlock, onClick = onUnlock)
  }
}

@Preview
@Composable
private fun PreviewAppLockScreen(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { AppLockScreen(onUnlock = {}) }
