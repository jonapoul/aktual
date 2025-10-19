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
package aktual.app.android

import aktual.app.nav.AktualAppContent
import aktual.core.di.ActivityKey
import aktual.core.di.ViewModelGraphProvider
import aktual.core.ui.AktualTheme
import aktual.core.ui.LocalViewModelGraphProvider
import aktual.core.ui.WithCompositionLocals
import aktual.core.ui.chooseSchemeType
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@Inject
@ActivityKey(AktualActivity::class)
@ContributesIntoMap(AppScope::class, binding<Activity>())
class AktualActivity(vmGraphProvider: ViewModelGraphProvider) : ComponentActivity() {
  override val defaultViewModelProviderFactory = vmGraphProvider

  private val viewModel by viewModels<AktualActivityViewModel> { vmGraphProvider }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.onDestroy()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.start()

    installSplashScreen()

    val transparent = Color.Transparent.value.toInt()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(transparent, transparent),
      navigationBarStyle = SystemBarStyle.auto(transparent, transparent),
    )

    setContent {
      Content(
        viewModel = viewModel,
        defaultViewModelProviderFactory = defaultViewModelProviderFactory,
      )
    }
  }
}

@Composable
private fun Content(
  viewModel: AktualActivityViewModel,
  defaultViewModelProviderFactory: ViewModelGraphProvider,
) {
  val regular by viewModel.regularSchemeType.collectAsStateWithLifecycle()
  val darkScheme by viewModel.darkSchemeType.collectAsStateWithLifecycle()
  val bottomBarState by viewModel.bottomBarState.collectAsStateWithLifecycle()
  val numberFormat by viewModel.numberFormat.collectAsStateWithLifecycle()
  val hideFraction by viewModel.hideFraction.collectAsStateWithLifecycle()
  val isPrivacyEnabled by viewModel.isPrivacyEnabled.collectAsStateWithLifecycle()
  val colorSchemeType = chooseSchemeType(regular, darkScheme)

  CompositionLocalProvider(
    LocalViewModelGraphProvider provides defaultViewModelProviderFactory,
  ) {
    WithCompositionLocals(
      isPrivacyEnabled = isPrivacyEnabled,
      format = numberFormat,
      hideFractions = hideFraction,
    ) {
      AktualTheme(colorSchemeType) {
        AktualAppContent(
          isPrivacyEnabled = isPrivacyEnabled,
          numberFormat = numberFormat,
          hideFraction = hideFraction,
          colorSchemeType = colorSchemeType,
          isServerUrlSet = viewModel.isServerUrlSet,
          loginToken = viewModel.loginToken,
          bottomBarState = bottomBarState,
        )
      }
    }
  }
}
