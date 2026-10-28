package aktual.app.nav

import aktual.core.icons.AktualIcons
import aktual.core.icons.LockClosed
import aktual.core.icons.material.Fingerprint
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.RounderCardShape
import aktual.core.ui.WavyBackground
import aktual.core.ui.WithCompositionLocals
import alakazam.compose.HorizontalSpacer
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
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
  Box(modifier.fillMaxSize()) {
    WavyBackground()
    LockedContent(onUnlock)
  }
}

@Composable
private fun LockedContent(onUnlock: () -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp),
    horizontalAlignment = CenterHorizontally,
  ) {
    VerticalSpacer(96.dp)

    Box(
      modifier =
        Modifier.size(88.dp)
          .background(colors.cardBackground, CircleShape)
          .border(1.dp, colors.cardBorder, CircleShape),
      contentAlignment = Center,
    ) {
      Icon(
        modifier = Modifier.size(40.dp),
        imageVector = AktualIcons.LockClosed,
        contentDescription = null,
        tint = colors.pageTextPositive,
      )
    }

    VerticalSpacer(28.dp)

    Text(
      text = Strings.appLockTitle,
      fontSize = textSizes.heading,
      fontWeight = W600,
      color = colors.pageText,
    )

    VerticalSpacer(8.dp)

    Text(
      text = Strings.appLockMessage,
      fontSize = textSizes.mediumPlus,
      textAlign = Center,
      color = colors.pageTextLight,
    )

    Spacer(Modifier.weight(1f))

    PrimaryTextButton(
      modifier = Modifier.fillMaxWidth().height(56.dp),
      text = Strings.appLockUnlock,
      shape = RounderCardShape,
      fontSize = textSizes.large,
      onClick = onUnlock,
      prefix = {
        Icon(
          modifier = Modifier.size(22.dp),
          imageVector = MaterialIcons.Fingerprint,
          contentDescription = null,
        )
        HorizontalSpacer(8.dp)
      },
    )

    VerticalSpacer(12.dp)

    Text(
      text = Strings.appLockPromptSubtitle,
      fontSize = textSizes.smallPlus,
      textAlign = Center,
      color = colors.pageTextLight,
    )

    VerticalSpacer(24.dp)
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
  @PreviewParameter(ColorParameters::class) colors: Colors,
) = PreviewWithColors(colors) { AppLockDisabledDialogContent(onDismiss = {}) }

@Preview
@Composable
private fun PreviewAppLockScreen(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { AppLockScreen(onUnlock = {}) }
