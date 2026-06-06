package aktual.budget.transactions.ui

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp

@Composable
internal fun CategoryHeader(modifier: Modifier = Modifier) =
  Row(
    modifier =
      modifier.height(IntrinsicSize.Min).fillMaxWidth().background(colors.tableHeaderBackground),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val dimens = LocalTableDimens.current

    // checkbox
    Spacer(Modifier.size(LocalMinimumInteractiveComponentSize.current))
    HorizontalSpacer(dimens.interColumn)

    // date
    CategoryHeaderText(Strings.transactionsHeaderDate)
    HorizontalSpacer(dimens.interColumn)

    // account
    CategoryHeaderText(Strings.transactionsHeaderAccount)
    HorizontalSpacer(dimens.interColumn)

    // payee
    CategoryHeaderText(Strings.transactionsHeaderPayee)
    HorizontalSpacer(dimens.interColumn)

    // notes
    CategoryHeaderText(Strings.transactionsHeaderNotes)
    HorizontalSpacer(dimens.interColumn)

    // category
    CategoryHeaderText(Strings.transactionsHeaderCategory)
    HorizontalSpacer(dimens.interColumn)

    // amount
    CategoryHeaderText(Strings.transactionsHeaderAmount, textAlign = TextAlign.End)
    HorizontalSpacer(dimens.interColumn)

    // button
    Spacer(Modifier.size(LocalMinimumInteractiveComponentSize.current))
  }

@Composable
private fun RowScope.CategoryHeaderText(
  text: String,
  modifier: Modifier = Modifier,
  textAlign: TextAlign = TextAlign.Start,
) =
  Text(
    modifier = modifier.weight(1f),
    text = text,
    textAlign = textAlign,
    fontSize = 14.sp,
    color = colors.tableHeaderText,
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
  )

@Preview
@Composable
private fun PreviewTransactionsSorting(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { CategoryHeader() }
