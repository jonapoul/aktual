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
package actual.test

import actual.core.model.ColorSchemeType
import actual.core.ui.LocalColorSchemeType
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewContextConfigurationEffect
import actual.core.ui.Theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.ComposeContentTestRule

fun ComposeContentTestRule.setAndroidThemedContent(
  theme: Theme = Theme.dark(),
  type: ColorSchemeType = ColorSchemeType.Dark,
  content: @Composable () -> Unit,
) = setContent {
  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalColorSchemeType provides type,
  ) {
    // Needed to initialise context for resource-fetching during tests
    PreviewContextConfigurationEffect()

    content()
  }
}
