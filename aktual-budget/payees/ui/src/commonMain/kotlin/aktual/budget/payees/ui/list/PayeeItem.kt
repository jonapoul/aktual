package aktual.budget.payees.ui.list

import aktual.budget.payees.vm.list.PayeeItem
import aktual.core.icons.AktualIcons
import aktual.core.icons.Bookmark
import aktual.core.icons.CheveronRight
import aktual.core.icons.LightBulb
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ChipSize
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/** Keep in sync with [ShimmerPayeeItem] */
@Composable
internal fun PayeeItem(
  payee: PayeeItem,
  onClick: () -> Unit,
  onClickRules: () -> Unit,
  modifier: Modifier = Modifier,
  query: String = "",
  // Only while the global learn-categories pref is on, as upstream
  showLearningOff: Boolean = false,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .clickable(onClick = onClick)
        .padding(ListPayeesDS.itemPadding),
    horizontalArrangement = Arrangement.spacedBy(ListPayeesDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    if (payee.isFavorite) {
      Icon(
        modifier = Modifier.size(ListPayeesDS.favoriteIconSize),
        imageVector = AktualIcons.Bookmark,
        contentDescription = Strings.payeesFavorite,
        tint = colors.pageText,
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
        color = if (payee.isTransfer) colors.pageTextLight else colors.pageText,
        maxLines = 1,
        overflow = Ellipsis,
      )

      Text(
        text = Plurals.payeesNumTransactions(payee.numTransactions, payee.numTransactions),
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }

    if (showLearningOff && !payee.learnsCategories) {
      Icon(
        modifier = Modifier.size(ListPayeesDS.learningIconSize),
        imageVector = AktualIcons.LightBulb,
        contentDescription = Strings.payeesLearningOff,
        tint = colors.errorText,
      )
    }

    RuleCountChip(numRules = payee.numRules, onClick = onClickRules)
  }
}

@Composable
internal fun RuleCountChip(numRules: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
  PayeeChip(
    modifier = modifier,
    text =
      if (numRules > 0) Plurals.payeesNumRules(numRules, numRules) else Strings.payeesCreateRule,
    onClick = onClick,
  )
}

@Composable
internal fun PayeeChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Chip(
    modifier = modifier,
    text = text,
    colors = ChipDefaults.colors(colors.noticeBackground, colors.noticeTextDark),
    textStyle = typography.bodySmall,
    trailingIcon = AktualIcons.CheveronRight,
    onClick = onClick,
  )
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
      ShimmerLine(
        style = typography.bodyLarge,
        barHeight = ListPayeesDS.shimmerNameHeight,
        modifier = Modifier.fillMaxWidth(fraction = 0.6f),
      )
      ShimmerLine(
        style = typography.bodySmall,
        barHeight = ListPayeesDS.shimmerSubtitleHeight,
        modifier = Modifier.fillMaxWidth(fraction = 0.3f),
      )
    }

    Box(
      modifier =
        Modifier.width(ListPayeesDS.shimmerChipWidth)
          .background(colors.pageText, ChipSize.Regular.shape)
          .padding(ChipSize.Regular.contentPadding),
    ) {
      Text(text = "", style = typography.bodySmall, maxLines = 1)
    }
  }
}

/** A bar vertically centred in the height of one line of [style], so it matches real text */
@Composable
private fun ShimmerLine(style: TextStyle, barHeight: Dp, modifier: Modifier = Modifier) {
  Box(modifier = modifier, contentAlignment = CenterStart) {
    Text(text = "", style = style, maxLines = 1)
    Box(modifier = Modifier.fillMaxWidth().height(barHeight).background(colors.pageText, CardShape))
  }
}

private class PayeeItemProvider : ColorParamsProvider<PayeeItem>(PayeesPreview.all)

@Preview
@Composable
private fun PreviewPayeeItem(
  @PreviewParameter(PayeeItemProvider::class) params: ColorParams<PayeeItem>,
) =
  PreviewWithColorParams(params) {
    PayeeItem(payee = this, onClick = {}, onClickRules = {}, showLearningOff = true)
  }

@Preview
@Composable
private fun PreviewShimmerPayeeItem(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerPayeeItem() }
