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
package aktual.budget.list.ui

import aktual.core.ui.AktualTypography
import aktual.core.ui.LocalTheme
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.Theme
import aktual.l10n.Strings
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
@Composable
internal fun ContentEmpty(
  onCreateBudgetInBrowser: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(40.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = Strings.budgetSuccessEmpty,
      textAlign = TextAlign.Center,
      style = AktualTypography.headlineLarge,
    )

    VerticalSpacer(20.dp)

    Text(
      text = Strings.budgetSuccessEmptySecond,
      color = theme.pageText,
      textAlign = TextAlign.Center,
      fontSize = 20.sp,
    )

    VerticalSpacer(30.dp)

    PrimaryTextButton(
      text = Strings.budgetSuccessEmptyLaunch,
      onClick = onCreateBudgetInBrowser,
    )
  }
}
