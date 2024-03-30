package dev.jonpoulton.actual.listbudgets.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.ActualScreenPreview
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualScreen
import dev.jonpoulton.actual.core.ui.PrimaryActualTextButton
import dev.jonpoulton.actual.core.ui.VerticalSpacer
import dev.jonpoulton.actual.core.res.R as ResR

@Stable
@Composable
internal fun ContentEmpty(
  modifier: Modifier = Modifier,
  colors: ActualColorScheme = LocalActualColorScheme.current,
  onCreateBudgetInBrowser: () -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(40.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(id = ResR.string.budget_success_empty),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.headlineLarge,
    )

    VerticalSpacer(20.dp)

    Text(
      text = stringResource(id = ResR.string.budget_success_empty_second),
      color = colors.pageText,
      textAlign = TextAlign.Center,
      fontFamily = ActualFontFamily,
      fontSize = 20.sp,
    )

    VerticalSpacer(30.dp)

    PrimaryActualTextButton(
      text = stringResource(id = ResR.string.budget_success_empty_launch),
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
