/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
