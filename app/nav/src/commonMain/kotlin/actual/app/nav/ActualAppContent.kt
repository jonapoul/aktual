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
package actual.app.nav

import actual.budget.model.NumberFormat
import actual.core.model.ColorSchemeType
import actual.core.model.LoginToken
import actual.core.ui.ActualTheme
import actual.core.ui.BottomBarState
import actual.core.ui.BottomNavBarSpacing
import actual.core.ui.WithCompositionLocals
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ActualAppContent(
  isPrivacyEnabled: Boolean,
  numberFormat: NumberFormat,
  hideFraction: Boolean,
  colorSchemeType: ColorSchemeType,
  isServerUrlSet: Boolean,
  loginToken: LoginToken?,
  bottomBarState: BottomBarState,
) {
  WithCompositionLocals(
    isPrivacyEnabled = isPrivacyEnabled,
    format = numberFormat,
    hideFractions = hideFraction,
  ) {
    ActualTheme(colorSchemeType) {
      ActualAppLayout(
        isServerUrlSet = isServerUrlSet,
        loginToken = loginToken,
        bottomBarState = bottomBarState,
      )
    }
  }
}

@Composable
private fun ActualAppLayout(
  isServerUrlSet: Boolean,
  loginToken: LoginToken?,
  bottomBarState: BottomBarState,
  modifier: Modifier = Modifier,
) = Box(
  modifier = modifier,
  contentAlignment = Alignment.BottomCenter,
) {
  ActualNavHost(
    modifier = Modifier.fillMaxWidth(),
    isServerUrlSet = isServerUrlSet,
    loginToken = loginToken,
  )

  if (bottomBarState is BottomBarState.Visible) {
    Column {
      BottomStatusBar(
        modifier = Modifier.wrapContentHeight(),
        state = bottomBarState,
      )
      BottomNavBarSpacing()
    }
  }
}
