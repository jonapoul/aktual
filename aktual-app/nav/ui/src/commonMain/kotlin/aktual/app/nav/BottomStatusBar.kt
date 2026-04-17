package aktual.app.nav

import aktual.budget.model.SyncState
import aktual.budget.model.SyncState.Inactive
import aktual.budget.model.SyncState.NoToken
import aktual.budget.model.SyncState.SyncFailed
import aktual.budget.model.SyncState.Syncing
import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudCheck
import aktual.core.icons.CloudWarning
import aktual.core.icons.material.Error
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Sync
import aktual.core.l10n.Strings
import aktual.core.model.PingState
import aktual.core.model.PingState.Failure
import aktual.core.model.PingState.Success
import aktual.core.model.PingState.Unknown
import aktual.core.theme.BottomBarThemeAttrs
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.BottomBarState.Visible
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FONT_SIZE = 10.sp
private val ICON_SIZE = 12.dp
private val PADDING = PaddingValues(vertical = 3.dp, horizontal = 8.dp)

@Composable
internal fun BottomStatusBar(
  state: Visible,
  attrs: BottomBarThemeAttrs,
  onClickSync: () -> Unit,
  onMeasureHeight: (Dp) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  density: Density = LocalDensity.current,
) {
  val onPositioned = { c: LayoutCoordinates ->
    with(density) { onMeasureHeight(c.size.height.toDp()) }
  }

  Row(
    modifier = modifier.padding(PADDING).fillMaxWidth().onGloballyPositioned(onPositioned),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    state.budgetName?.let { name ->
      Text(
        text = loadedString(name),
        fontSize = FONT_SIZE,
        color = attrs.foreground(theme),
        maxLines = 1,
        overflow = Ellipsis,
      )

      HorizontalSpacer(weight = 1f)

      SyncState(state.syncState, attrs, onClickSync)
    }

    HorizontalSpacer(weight = 1f)

    PingState(state.pingState)
  }
}

@Composable
private fun PingState(
  state: PingState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(5.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val text = state.text()
    val tint = state.tint(theme)
    Image(
      modifier = Modifier.size(ICON_SIZE),
      imageVector = state.icon(),
      contentDescription = text,
      colorFilter = ColorFilter.tint(tint),
    )

    Text(text = text, fontSize = FONT_SIZE, color = tint, maxLines = 1, overflow = Ellipsis)
  }
}

@Stable
private fun PingState.icon() =
  when (this) {
    Success -> AktualIcons.CloudCheck
    Failure -> AktualIcons.CloudWarning
    Unknown -> AktualIcons.CloudWarning
  }

@Composable
private fun PingState.text() =
  when (this) {
    Success -> Strings.connectionConnected
    Failure -> Strings.connectionDisconnected
    Unknown -> Strings.connectionUnknown
  }

@Stable
private fun PingState.tint(theme: Theme) =
  when (this) {
    Success -> theme.noticeText
    Failure -> theme.warningText
    Unknown -> theme.pageTextSubdued
  }

@Composable
private fun loadedString(budgetName: String): AnnotatedString {
  val budgetLoaded = Strings.budgetLoaded
  return remember(budgetName, budgetLoaded) {
    buildAnnotatedString {
      append(budgetLoaded)
      append("  ")
      withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(budgetName) }
    }
  }
}

@Composable
private fun SyncState(
  state: SyncState,
  attrs: BottomBarThemeAttrs,
  onClickSync: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val text = state.text()
  val tint = state.tint(theme, attrs)

  Row(
    modifier =
      modifier
        .padding(horizontal = 4.dp)
        .clickable(enabled = state is Inactive, onClick = onClickSync),
    horizontalArrangement = Arrangement.spacedBy(5.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      modifier = Modifier.size(ICON_SIZE),
      imageVector = state.icon(),
      contentDescription = text,
      colorFilter = ColorFilter.tint(tint),
    )

    Text(text = text, fontSize = FONT_SIZE, color = tint, maxLines = 1, overflow = Ellipsis)
  }
}

@Stable
private fun SyncState.icon() =
  when (this) {
    NoToken -> MaterialIcons.Error
    is SyncFailed -> MaterialIcons.Error
    Syncing -> MaterialIcons.Sync
    Inactive -> MaterialIcons.Sync
  }

@Composable
private fun SyncState.text() =
  when (this) {
    NoToken -> Strings.syncNoToken
    is SyncFailed -> Strings.syncFailed
    Syncing -> Strings.syncSyncing
    Inactive -> Strings.sync
  }

@Stable
private fun SyncState.tint(theme: Theme, attrs: BottomBarThemeAttrs) =
  when (this) {
    NoToken -> theme.warningText
    is SyncFailed -> theme.errorText
    Syncing -> theme.reportsBlue
    Inactive -> attrs.foreground(theme)
  }

@Preview
@Composable
private fun PreviewBottomBar(
  @PreviewParameter(BottomBarProvider::class) params: ThemedParams<BottomBarParams>
) =
  PreviewWithThemedParams(params) {
    BottomStatusBar(
      modifier = Modifier.fillMaxWidth(),
      state = Visible(state, syncState, budgetName),
      onMeasureHeight = {},
      onClickSync = {},
      attrs =
        BottomBarThemeAttrs(
          shouldBlurOnRootLevel = true,
          background = { cardBackground },
          foreground = { pageText },
        ),
    )
  }

private data class BottomBarParams(
  val state: PingState,
  val syncState: SyncState = Inactive,
  val budgetName: String? = null,
)

@Suppress("StringLiteralDuplication")
private class BottomBarProvider :
  ThemedParameterProvider<BottomBarParams>(
    BottomBarParams(state = Success, budgetName = "My Budget"),
    BottomBarParams(state = Success, syncState = NoToken, budgetName = "My Budget"),
    BottomBarParams(state = Success, syncState = SyncFailed("abc"), budgetName = "My Budget"),
    BottomBarParams(state = Success, syncState = Syncing, budgetName = "My Budget"),
    BottomBarParams(state = Failure),
    BottomBarParams(state = Unknown),
  )
