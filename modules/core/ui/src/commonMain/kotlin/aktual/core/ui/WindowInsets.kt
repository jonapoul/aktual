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
package aktual.core.ui

import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
expect fun SetStatusBarColors(
  theme: Theme = LocalTheme.current,
  statusBarColor: Color = theme.mobileHeaderBackground,
  navigationBarColor: Color = theme.pageBackground,
)

// space to block out the bottom navigation bar, so we don't need to adjust layouts to account for it
@Composable
fun BottomNavBarSpacing(modifier: Modifier = Modifier) = VerticalSpacer(
  modifier = modifier,
  height = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
)
