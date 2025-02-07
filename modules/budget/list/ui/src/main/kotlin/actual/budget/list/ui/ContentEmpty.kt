package actual.budget.list.ui

import actual.budget.list.res.BudgetListStrings
import actual.core.ui.ActualFontFamily
import actual.core.ui.ActualScreenPreview
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewActualScreen
import actual.core.ui.PrimaryActualTextButton
import actual.core.ui.Theme
import actual.core.ui.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
      text = BudgetListStrings.budgetSuccessEmpty,
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.headlineLarge,
    )

    VerticalSpacer(20.dp)

    Text(
      text = BudgetListStrings.budgetSuccessEmptySecond,
      color = theme.pageText,
      textAlign = TextAlign.Center,
      fontFamily = ActualFontFamily,
      fontSize = 20.sp,
    )

    VerticalSpacer(30.dp)

    PrimaryActualTextButton(
      text = BudgetListStrings.budgetSuccessEmptyLaunch,
      onClick = onCreateBudgetInBrowser,
    )
  }
}

@ActualScreenPreview
@Composable
private fun Empty() = PreviewActualScreen {
  ContentEmpty(
    onCreateBudgetInBrowser = {},
  )
}
