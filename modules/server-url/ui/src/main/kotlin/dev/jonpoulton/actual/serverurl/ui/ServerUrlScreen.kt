package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.jonpoulton.actual.core.ui.ActualColors
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.PreviewActual
import dev.jonpoulton.actual.serverurl.vm.ServerUrlViewModel
import dev.jonpoulton.actual.core.res.R as ResR

@Suppress("UnusedParameter")
@Composable
fun ServerUrlScreen(
  navController: NavHostController,
  viewModel: ServerUrlViewModel = hiltViewModel(),
) {
  val appVersion by viewModel.appVersion.collectAsStateWithLifecycle(initialValue = null)
  val serverVersion by viewModel.serverVersion.collectAsStateWithLifecycle(initialValue = null)

  ServerUrlScreenImpl(
    appVersion = appVersion,
    serverVersion = serverVersion,
  )
}

@Composable
private fun ServerUrlScreenImpl(
  appVersion: String?,
  serverVersion: String?,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
          Text(
            text = stringResource(id = ResR.string.server_url_toolbar),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Content(
      modifier = Modifier.padding(innerPadding),
      appVersion = appVersion,
      serverVersion = serverVersion,
    )
  }
}

@Composable
private fun Content(
  modifier: Modifier,
  appVersion: String?,
  serverVersion: String?,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .wrapContentWidth()
        .wrapContentHeight()
        .padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        modifier = Modifier.padding(bottom = 15.dp),
        text = stringResource(id = ResR.string.server_url_title),
        fontFamily = ActualFontFamily,
        fontWeight = FontWeight.W700,
        color = ActualColors.purple200,
        fontSize = 25.sp,
      )

      Text(
        text = stringResource(id = ResR.string.server_url_message),
        fontFamily = ActualFontFamily,
        color = ActualColors.navy150,
      )

      TextField(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .padding(vertical = 20.dp),
        value = "",
        placeholder = { Text(text = "https://example.com") },
        onValueChange = { /* TODO: Handle input */ },
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OkButton(
          onClick = {},
        )
      }
    }

    VersionsText(
      modifier = Modifier.align(Alignment.BottomEnd),
      appVersion,
      serverVersion,
    )
  }
}

@PreviewThemes
@Composable
private fun Preview() = PreviewActual {
  ServerUrlScreenImpl(
    serverVersion = "v24.3.0",
    appVersion = "v1.2.3",
  )
}
