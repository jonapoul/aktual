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

import actual.core.ui.ActualTypography
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun BuildStateItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  onClick: (() -> Unit)? = null,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(ItemHeight)
      .background(Color.Transparent, CardShape)
      .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
      .padding(horizontal = ItemPadding),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.padding(ItemPadding),
      imageVector = icon,
      contentDescription = title,
      tint = theme.pageText,
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(ItemPadding),
    ) {
      Text(
        modifier = Modifier.testTag(Tags.BuildStateItemTitle),
        text = title,
        style = ActualTypography.bodyLarge,
        color = theme.pageText,
      )
      Text(
        modifier = Modifier.testTag(Tags.BuildStateItemValue),
        text = subtitle,
        style = ActualTypography.labelMedium,
        color = theme.pageTextSubdued,
      )
    }
  }
}

private val ItemPadding = 8.dp
private val ItemHeight = 50.dp
