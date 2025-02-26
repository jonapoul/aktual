package actual.url.ui

import actual.account.login.nav.LoginNavRoute
import actual.core.colorscheme.ColorSchemeType
import actual.core.res.CoreStrings
import actual.core.ui.ActualFontFamily
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.PrimaryTextButtonWithLoading
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.VersionsText
import actual.core.ui.VerticalSpacer
import actual.core.ui.WavyBackground
import actual.core.ui.debugNavigate
import actual.core.ui.transparentTopAppBarColors
import actual.core.versions.ActualVersions
import actual.log.Logger
import actual.url.model.Protocol
import actual.url.res.ServerUrlStrings
import actual.url.vm.NavDestination
import actual.url.vm.ServerUrlViewModel
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.channels.consumeEach

@Composable
fun ServerUrlScreen(
  navController: NavHostController,
  viewModel: ServerUrlViewModel = hiltViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
  val protocol by viewModel.protocol.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val themeType by viewModel.themeType.collectAsStateWithLifecycle()

  DisposableEffect(Unit) {
    onDispose {
      viewModel.clearState()
    }
  }

  val context = LocalContext.current
  val activity = remember { context as? Activity ?: error("$context isn't an activity?") }

  LaunchedEffect(Unit) {
    viewModel.navDestination.consumeEach { destination ->
      when (destination) {
        NavDestination.Back -> activity.finish()
        NavDestination.ToBootstrap -> Logger.w("Not implemented bootstrap yet!")
        NavDestination.ToLogin -> navController.debugNavigate(LoginNavRoute)
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
    themeType = themeType,
    onAction = { action ->
      when (action) {
        ServerUrlAction.ConfirmUrl -> viewModel.onClickConfirm()
        ServerUrlAction.NavBack -> viewModel.onClickBack()
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
  versions: ActualVersions,
  isEnabled: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  themeType: ColorSchemeType,
  onAction: (ServerUrlAction) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = {
          IconButton(onClick = { onAction(ServerUrlAction.NavBack) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = CoreStrings.navBack,
            )
          }
        },
        title = { },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground(themeType)

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
  versions: ActualVersions,
  isEnabled: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  onAction: (ServerUrlAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
      .padding(horizontal = 16.dp)
      .wrapContentWidth()
      .wrapContentHeight(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.Start,
  ) {
    Text(
      text = ServerUrlStrings.serverUrlTitle,
      style = MaterialTheme.typography.headlineLarge,
    )

    VerticalSpacer(height = 15.dp)

    Text(
      text = ServerUrlStrings.serverUrlMessage,
      color = theme.tableRowHeaderText,
      style = MaterialTheme.typography.bodyLarge,
    )

    VerticalSpacer(height = 20.dp)

    InputFields(
      modifier = Modifier.fillMaxWidth(),
      url = url,
      protocol = protocol,
      onAction = onAction,
    )

    VerticalSpacer(height = 20.dp)

    PrimaryTextButtonWithLoading(
      modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth(),
      text = ServerUrlStrings.serverUrlConfirm,
      isLoading = isLoading,
      isEnabled = isEnabled,
      onClick = { onAction(ServerUrlAction.ConfirmUrl) },
    )

    if (errorMessage != null) {
      VerticalSpacer(20.dp)

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = errorMessage,
        fontFamily = ActualFontFamily,
        color = theme.errorText,
        textAlign = TextAlign.Center,
      )
    }

    Spacer(
      modifier = Modifier.weight(1f),
    )

    Box(
      modifier = Modifier.fillMaxWidth(),
    ) {
      VersionsText(
        modifier = Modifier.align(Alignment.BottomEnd),
        versions = versions,
      )
    }
  }
}

@ScreenPreview
@Composable
private fun Regular() = PreviewScreen { type ->
  ServerUrlScaffold(
    url = "",
    protocol = Protocol.Https,
    versions = ActualVersions.Dummy,
    isEnabled = true,
    isLoading = false,
    themeType = type,
    onAction = {},
    errorMessage = null,
  )
}

@ScreenPreview
@Composable
private fun WithErrorMessage() = PreviewScreen { type ->
  ServerUrlScaffold(
    url = "my.server.com:1234/path",
    protocol = Protocol.Http,
    versions = ActualVersions.Dummy,
    isEnabled = true,
    isLoading = true,
    themeType = type,
    onAction = {},
    errorMessage = "Hello this is an error message, split over multiple lines so you can see how it behaves",
  )
}
