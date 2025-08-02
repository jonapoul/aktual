package actual.budget.list.ui

import actual.core.ui.ActualFontFamily
import actual.core.ui.ActualTypography
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.PrimaryTextButton
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.l10n.Strings
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
      style = ActualTypography.headlineLarge,
    )

    VerticalSpacer(20.dp)

    Text(
      text = Strings.budgetSuccessEmptySecond,
      color = theme.pageText,
      textAlign = TextAlign.Center,
      fontFamily = ActualFontFamily,
      fontSize = 20.sp,
    )

    VerticalSpacer(30.dp)

    PrimaryTextButton(
      text = Strings.budgetSuccessEmptyLaunch,
      onClick = onCreateBudgetInBrowser,
    )
  }
}

@ScreenPreview
@Composable
private fun Empty() = PreviewScreen {
  ContentEmpty(
    onCreateBudgetInBrowser = {},
  )
}
