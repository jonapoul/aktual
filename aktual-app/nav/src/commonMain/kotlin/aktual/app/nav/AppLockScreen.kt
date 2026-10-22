package aktual.app.nav

import aktual.core.l10n.Drawables
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppLockContent(
  viewModel: RootViewModel,
  isUnavailable: Boolean,
  onUnlock: (AppLockPrompt) -> Unit,
  onDisable: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val prompt = rememberAppLockPrompt()

  // Saveable so a rotation doesn't show the prompt a second time
  var hasPrompted by rememberSaveable { mutableStateOf(false) }
  SideEffect {
    if (!hasPrompted) {
      hasPrompted = true
      onUnlock(prompt)
    }
  }

  val isSystemDark = isSystemInDarkTheme()
  SideEffect(isSystemDark) { viewModel.updateSystemDarkTheme(isSystemDark) }
  val colors by viewModel.colors.collectAsStateWithLifecycle()

  WithCompositionLocals {
    AktualTheme(colors) {
      AppLockScreen(onUnlock = { onUnlock(prompt) }, modifier)
      if (isUnavailable) {
        AppLockDisabledDialog(onDismiss = onDisable)
      }
    }
  }
}

@Composable
internal fun AppLockScreen(onUnlock: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize().background(colors.tableBackground).safeDrawingPadding(),
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

@Composable
private fun AppLockDisabledDialog(onDismiss: () -> Unit) {
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
    content = { AppLockDisabledDialogContent(onDismiss) },
  )
}

@Composable
private fun AppLockDisabledDialogContent(onDismiss: () -> Unit) {
  AktualAlertDialogContent(
    title = Strings.appLockDisabledTitle,
    buttons = { TextButton(onClick = onDismiss) { Text(Strings.appLockDisabledOk) } },
  ) {
    Text(Strings.appLockDisabledMessage)
  }
}

@Preview
@Composable
private fun PreviewAppLockDisabledDialog(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) = PreviewWithColors(colors) { AppLockDisabledDialogContent(onDismiss = {}) }

@Preview
@Composable
private fun PreviewAppLockScreen(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { AppLockScreen(onUnlock = {}) }
