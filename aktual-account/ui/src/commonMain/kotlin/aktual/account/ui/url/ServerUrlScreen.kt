package aktual.account.ui.url

import aktual.account.vm.NavDestination
import aktual.account.vm.ServerUrlViewModel
import aktual.app.nav.InfoNavigator
import aktual.app.nav.LoginNavigator
import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.AktualVersions
import aktual.core.model.Protocol
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BasicIconButton
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.PrimaryTextButtonWithLoading
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.VersionsText
import aktual.core.ui.WavyBackground
import aktual.core.ui.normalIconButton
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import logcat.logcat

@Composable
fun ServerUrlScreen(
  toLogin: LoginNavigator,
  toInfo: InfoNavigator,
  viewModel: ServerUrlViewModel = metroViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
  val protocol by viewModel.protocol.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

  DisposableEffect(Unit) { onDispose { viewModel.clearState() } }

  LaunchedEffect(Unit) {
    // Don't consumeEach here, it'll cancel the channel when we nav forward
    for (destination in viewModel.navDestination) {
      when (destination) {
        NavDestination.ToBootstrap -> logcat.w { "Not implemented bootstrap yet!" }
        NavDestination.ToLogin -> toLogin()
        NavDestination.ToAbout -> toInfo()
      }
    }
  }

  ServerUrlScaffold(
    url = enteredUrl,
    protocol = protocol,
    versions = versions,
    isEnabled = isEnabled,
    isLoading = isLoading,
    errorMessage = errorMessage,
    onAction = { action ->
      when (action) {
        ServerUrlAction.ConfirmUrl -> viewModel.onClickConfirm()
        ServerUrlAction.OpenAbout -> viewModel.onClickAbout()
        is ServerUrlAction.EnterUrl -> viewModel.onEnterUrl(action.url)
        is ServerUrlAction.SelectProtocol -> viewModel.onSelectProtocol(action.protocol)
        is ServerUrlAction.UseDemoServer -> viewModel.onUseDemoServer()
      }
    },
  )
}

@Composable
private fun ServerUrlScaffold(
  url: String,
  protocol: Protocol,
  versions: AktualVersions,
  isEnabled: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  onAction: (ServerUrlAction) -> Unit,
) {
  val theme = LocalTheme.current

  Scaffold(
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        title = {},
        actions = {
          BasicIconButton(
            modifier = Modifier.padding(horizontal = 5.dp),
            onClick = { onAction(ServerUrlAction.OpenAbout) },
            imageVector = MaterialIcons.Info,
            contentDescription = Strings.serverUrlMenuAbout,
            colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
          )
        },
      )
    }
  ) { innerPadding ->
    Box {
      WavyBackground()

      ServerUrlContent(
        modifier = Modifier.padding(innerPadding),
        url = url,
        protocol = protocol,
        versions = versions,
        isEnabled = isEnabled,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Stable
@Composable
private fun ServerUrlContent(
  url: String,
  protocol: Protocol,
  versions: AktualVersions,
  isEnabled: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  onAction: (ServerUrlAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.padding(horizontal = 16.dp).wrapContentWidth().wrapContentHeight(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.Start,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
      Text(text = Strings.serverUrlTitle, style = AktualTypography.headlineLarge)

      Text(
        text = Strings.serverUrlMessage,
        color = theme.tableRowHeaderText,
        style = AktualTypography.bodyLarge,
      )

      InputFields(
        modifier = Modifier.fillMaxWidth(),
        url = url,
        protocol = protocol,
        onAction = onAction,
        theme = theme,
      )

      PrimaryTextButtonWithLoading(
        modifier = Modifier.padding(5.dp).fillMaxWidth(),
        text = Strings.serverUrlConfirm,
        isLoading = isLoading,
        isEnabled = isEnabled,
        onClick = { onAction(ServerUrlAction.ConfirmUrl) },
      )

      if (errorMessage != null) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = errorMessage,
          color = theme.errorText,
          textAlign = TextAlign.Center,
        )
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    Box(modifier = Modifier.fillMaxWidth()) {
      VersionsText(modifier = Modifier.align(Alignment.BottomEnd), versions = versions)
    }

    BottomStatusBarSpacing()
    BottomNavBarSpacing()
  }
}

@Composable
@PortraitPreview
private fun PreviewServerUrlScaffold(
  @PreviewParameter(ServerUrlScaffoldProvider::class) params: ThemedParams<ServerUrlScaffoldParams>
) =
  PreviewWithTheme(params.theme) {
    ServerUrlScaffold(
      url = params.data.url,
      protocol = params.data.protocol,
      versions = AktualVersions.Dummy,
      isEnabled = true,
      isLoading = params.data.isLoading,
      onAction = {},
      errorMessage = params.data.errorMessage,
    )
  }

private data class ServerUrlScaffoldParams(
  val url: String,
  val protocol: Protocol,
  val isLoading: Boolean,
  val errorMessage: String?,
)

private class ServerUrlScaffoldProvider :
  ThemedParameterProvider<ServerUrlScaffoldParams>(
    ServerUrlScaffoldParams(
      url = "",
      protocol = Protocol.Https,
      isLoading = false,
      errorMessage = null,
    ),
    ServerUrlScaffoldParams(
      url = "my.server.com:1234/path",
      protocol = Protocol.Http,
      isLoading = true,
      errorMessage =
        "Hello this is an error message, split over multiple lines so you can see how it behaves",
    ),
  )
