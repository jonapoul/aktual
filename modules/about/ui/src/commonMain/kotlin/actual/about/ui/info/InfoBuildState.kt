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
package actual.about.ui.info

import actual.about.vm.BuildState
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.core.ui.defaultHazeStyle
import actual.l10n.Strings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
internal fun InfoBuildState(
  buildState: BuildState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  hazeState: HazeState = remember { HazeState() },
  hazeStyle: HazeStyle = defaultHazeStyle(theme),
) {
  Column(modifier) {
    BuildStateItem(
      modifier = Modifier
        .padding(ItemMargin)
        .hazeEffect(hazeState, hazeStyle),
      icon = Icons.Filled.Apps,
      title = Strings.infoAppVersion,
      subtitle = buildState.versions.app,
    )

    BuildStateItem(
      modifier = Modifier
        .testTag(Tags.ServerVersionText)
        .padding(ItemMargin)
        .hazeEffect(hazeState, hazeStyle),
      icon = Icons.Filled.Cloud,
      title = Strings.infoServerVersion,
      subtitle = buildState.versions.server ?: Strings.infoServerVersionUnknown,
    )

    BuildStateItem(
      modifier = Modifier
        .padding(ItemMargin)
        .hazeEffect(hazeState, hazeStyle),
      icon = Icons.Filled.CalendarToday,
      title = Strings.infoDate,
      subtitle = buildState.buildDate,
    )
  }
}

private val ItemMargin = PaddingValues(horizontal = 6.dp, vertical = 3.dp)
