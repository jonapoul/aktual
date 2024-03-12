package dev.jonpoulton.actual.login.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import dev.jonpoulton.actual.core.ui.PreviewActual
import dev.jonpoulton.actual.login.vm.LoginViewModel

@Suppress("UnusedParameter")
@Composable
fun LoginScreen(
  navController: NavHostController,
  viewModel: LoginViewModel = hiltViewModel(),
) {
  LoginScreenImpl(
    // TBC
  )
}

@Composable
private fun LoginScreenImpl(
  // TBC
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      MediumTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
          Text(
            "Medium Top App Bar",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        navigationIcon = {
          IconButton(onClick = { /* do something */ }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Localized description",
            )
          }
        },
        actions = {
          IconButton(onClick = { /* do something */ }) {
            Icon(
              imageVector = Icons.Filled.Menu,
              contentDescription = "Localized description",
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

@PreviewThemes
@Composable
private fun Preview() = PreviewActual {
  LoginScreenImpl()
}
