/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.account.ui.url

import aktual.account.vm.NavDestination
import aktual.account.vm.ServerUrlViewModel
import aktual.core.model.AktualVersions
import aktual.core.model.Protocol
import aktual.core.ui.AktualTypography
import aktual.core.ui.BasicIconButton
import aktual.core.ui.LocalTheme
import aktual.core.ui.PrimaryTextButtonWithLoading
import aktual.core.ui.Theme
import aktual.core.ui.VersionsText
import aktual.core.ui.WavyBackground
import aktual.core.ui.appCloser
import aktual.core.ui.metroViewModel
import aktual.core.ui.normalIconButton
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
import alakazam.kotlin.compose.VerticalSpacer
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.channels.consumeEach
import logcat.logcat

@Composable
fun ServerUrlScreen(
  nav: ServerUrlNavigator,
  viewModel: ServerUrlViewModel = metroViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
  val protocol by viewModel.protocol.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

  DisposableEffect(Unit) {
    onDispose {
      viewModel.clearState()
    }
  }

  val closeApp = appCloser()

  LaunchedEffect(Unit) {
    viewModel.navDestination.consumeEach { destination ->
      when (destination) {
        NavDestination.Back -> closeApp()
        NavDestination.ToBootstrap -> logcat.w { "Not implemented bootstrap yet!" }
        NavDestination.ToLogin -> nav.toLogin()
        NavDestination.ToAbout -> nav.toAbout()
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
        ServerUrlAction.NavBack -> viewModel.onClickBack()
        ServerUrlAction.OpenAbout -> viewModel.onClickAbout()
        is ServerUrlAction.EnterUrl -> viewModel.onEnterUrl(action.url)
        is ServerUrlAction.SelectProtocol -> viewModel.onSelectProtocol(action.protocol)
        is ServerUrlAction.UseDemoServer -> viewModel.onUseDemoServer()
      }
    },
  )
}

@Composable
internal fun ServerUrlScaffold(
  url: String,
  protocol: Protocol,
  versions: AktualVersions,
  isEnabled: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
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
              contentDescription = Strings.navBack,
            )
          }
        },
        title = { },
        scrollBehavior = scrollBehavior,
        actions = {
          BasicIconButton(
            modifier = Modifier.padding(horizontal = 5.dp),
            onClick = { onAction(ServerUrlAction.OpenAbout) },
            imageVector = Icons.Filled.Info,
            contentDescription = Strings.serverUrlMenuAbout,
            colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
          )
        },
      )
    },
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
    modifier = modifier
      .padding(horizontal = 16.dp)
      .wrapContentWidth()
      .wrapContentHeight(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.Start,
  ) {
    Text(
      text = Strings.serverUrlTitle,
      style = AktualTypography.headlineLarge,
    )

    VerticalSpacer(height = 15.dp)

    Text(
      text = Strings.serverUrlMessage,
      color = theme.tableRowHeaderText,
      style = AktualTypography.bodyLarge,
    )

    VerticalSpacer(height = 20.dp)

    InputFields(
      modifier = Modifier.fillMaxWidth(),
      url = url,
      protocol = protocol,
      onAction = onAction,
      theme = theme,
    )

    VerticalSpacer(height = 20.dp)

    PrimaryTextButtonWithLoading(
      modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth(),
      text = Strings.serverUrlConfirm,
      isLoading = isLoading,
      isEnabled = isEnabled,
      onClick = { onAction(ServerUrlAction.ConfirmUrl) },
    )

    if (errorMessage != null) {
      VerticalSpacer(20.dp)

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = errorMessage,
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
