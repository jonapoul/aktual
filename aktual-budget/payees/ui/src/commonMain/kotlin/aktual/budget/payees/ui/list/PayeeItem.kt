package aktual.budget.payees.ui.list

import aktual.budget.payees.vm.list.PayeeItem
import aktual.core.icons.material.Bookmark
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import aktual.core.ui.rememberHighlighted
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/** Keep in sync with [ShimmerPayeeItem] */
@Composable
internal fun PayeeItem(
  payee: PayeeItem,
  onClickRules: () -> Unit,
  modifier: Modifier = Modifier,
  query: String = "",
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .padding(ListPayeesDS.itemPadding),
    horizontalArrangement = Arrangement.spacedBy(ListPayeesDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    if (payee.isFavorite) {
      Icon(
        modifier = Modifier.size(ListPayeesDS.favoriteIconSize),
        imageVector = MaterialIcons.Bookmark,
        contentDescription = Strings.payeesFavorite,
        tint = colors.tableText,
      )
    }

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(ListPayeesDS.itemContentSpacing),
    ) {
      Text(
        text =
          rememberHighlighted(
            text = if (payee.isTransfer) Strings.payeesTransfer(payee.name) else payee.name,
            query = query,
          ),
        style = typography.bodyLarge,
        color = if (payee.isTransfer) colors.tableTextLight else colors.tableText,
        maxLines = 1,
        overflow = Ellipsis,
      )

      Text(
        text = Plurals.payeesNumTransactions(payee.numTransactions, payee.numTransactions),
        style = typography.bodySmall,
        color = colors.tableTextSubdued,
      )
    }

    RuleCountChip(numRules = payee.numRules, onClick = onClickRules)
  }
}

@Composable
private fun RuleCountChip(numRules: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .clip(CardShape)
        .background(colors.noticeBackground)
        .clickable(onClick = onClick)
        .padding(ListPayeesDS.ruleChipPadding),
    contentAlignment = Center,
  ) {
    Text(
      text =
        if (numRules > 0) Plurals.payeesNumRules(numRules, numRules) else Strings.payeesCreateRule,
      style = typography.bodySmall,
      color = colors.noticeTextDark,
    )
  }
}

/** Keep in sync with [PayeeItem] */
@Composable
internal fun ShimmerPayeeItem(modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .padding(ListPayeesDS.itemPadding)
        .shimmer(rememberShimmer(Window)),
    horizontalArrangement = Arrangement.spacedBy(ListPayeesDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(ListPayeesDS.itemContentSpacing),
    ) {
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.6f)
            .height(ListPayeesDS.shimmerNameHeight)
            .background(colors.pageText, CardShape),
      )
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.3f)
            .height(ListPayeesDS.shimmerSubtitleHeight)
            .background(colors.pageText, CardShape),
      )
    }

    Box(
      modifier =
        Modifier.size(
            width = ListPayeesDS.shimmerChipWidth,
            height = ListPayeesDS.shimmerChipHeight,
          )
          .background(colors.pageText, CardShape),
    )
  }
}

private class PayeeItemProvider : ColoredParameterProvider<PayeeItem>(PayeesPreview.all)

@Preview
@Composable
private fun PreviewPayeeItem(
  @PreviewParameter(PayeeItemProvider::class) params: ColoredParams<PayeeItem>,
) = PreviewWithColoredParams(params) { PayeeItem(payee = this, onClickRules = {}) }

@Preview
@Composable
private fun PreviewShimmerPayeeItem(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerPayeeItem() }
