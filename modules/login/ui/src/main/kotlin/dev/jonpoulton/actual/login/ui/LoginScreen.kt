package dev.jonpoulton.actual.login.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import dev.jonpoulton.actual.core.ui.PreviewActualScreen
import dev.jonpoulton.actual.login.vm.LoginViewModel
import dev.jonpoulton.actual.core.res.R as ResR

@Suppress("UnusedParameter")
@Composable
fun LoginScreen(
  navController: NavHostController,
  viewModel: LoginViewModel = hiltViewModel(),
) {
  var showAboutDialog by remember { mutableStateOf(false) }

  LoginScreenImpl(
    onClickAbout = { showAboutDialog = true },
  )
}

@Composable
private fun LoginScreenImpl(
  onClickAbout: () -> Unit,
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
            text = stringResource(id = ResR.string.login_toolbar),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        actions = {
          IconButton(onClick = onClickAbout) {
            Icon(
              imageVector = Icons.Filled.MoreVert,
              contentDescription = stringResource(id = ResR.string.login_about),
            )
          }
        },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Text(
      modifier = Modifier.padding(innerPadding),
      text = "Hello world",
    )
  }
}

@PreviewActualScreen
@Composable
private fun Preview() = PreviewActualScreen {
  LoginScreenImpl(
    onClickAbout = {},
  )
}
