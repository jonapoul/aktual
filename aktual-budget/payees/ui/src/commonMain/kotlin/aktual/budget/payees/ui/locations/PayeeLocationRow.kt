package aktual.budget.payees.ui.locations

import aktual.budget.payees.ui.text
import aktual.budget.payees.vm.locations.PayeeLocationItem
import aktual.core.icons.AktualIcons
import aktual.core.icons.Location
import aktual.core.icons.Trash
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.LocalDateFormatter
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import aktual.core.ui.ShimmerLine
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.contrastingTextColor
import aktual.core.ui.rememberHighlighted
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SwipeablePayeeLocationRow(
  location: PayeeLocationItem,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onClick: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
  query: String = "",
) {
  val deleteBackground = colors.errorText
  SwipeToReveal(
    actions =
      persistentListOf(
        SwipeAction(
          text = Strings.payeesDelete,
          icon = AktualIcons.Trash,
          background = deleteBackground,
          foreground = deleteBackground.contrastingTextColor(),
          onClick = {
            onOpenChange(false)
            onDelete()
          },
        ),
      ),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    PayeeLocationRow(location = location, onClick = onClick, query = query)
  }
}

/** Keep in sync with [ShimmerPayeeLocationRow] */
@Composable
private fun PayeeLocationRow(
  location: PayeeLocationItem,
  onClick: () -> Unit,
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
        .clickable(onClickLabel = Strings.payeesEditLocationOpen, onClick = onClick)
        .padding(PayeeLocationsDS.itemPadding),
    horizontalArrangement = Arrangement.spacedBy(PayeeLocationsDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      imageVector = AktualIcons.Location,
      contentDescription = null,
      tint = colors.pageTextSubdued,
    )

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(PayeeLocationsDS.itemContentSpacing),
    ) {
      Text(
        text = rememberHighlighted(text = location.payeeName, query = query),
        style = typography.bodyLarge,
        color = colors.pageText,
        maxLines = 1,
        overflow = Ellipsis,
      )

      Row {
        val date = location.date
        if (date != null) {
          Text(
            modifier = Modifier.wrapContentWidth(),
            text = LocalDateFormatter.current.format(date),
            style = typography.bodySmall,
            color = colors.pageTextSubdued,
            overflow = Ellipsis,
          )
        }

        Text(
          modifier = Modifier.weight(1f),
          text = location.coordinates.text(),
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
          textAlign = End,
          overflow = Ellipsis,
        )
      }
    }
  }
}

/** Keep in sync with [PayeeLocationRow] */
@Composable
internal fun ShimmerPayeeLocationRow(modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .padding(PayeeLocationsDS.itemPadding)
        .shimmer(rememberShimmer(Window)),
    horizontalArrangement = Arrangement.spacedBy(PayeeLocationsDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Box(
      Modifier.size(PayeeLocationsDS.shimmerIconSize).background(colors.pageText, RowShape),
    )

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(PayeeLocationsDS.itemContentSpacing),
    ) {
      ShimmerLine(typography.bodyLarge, Modifier.fillMaxWidth(fraction = 0.6f))
      ShimmerLine(typography.bodySmall, Modifier.fillMaxWidth(fraction = 0.3f))
      ShimmerLine(typography.bodySmall, Modifier.fillMaxWidth(fraction = 0.4f))
    }
  }
}

private class PayeeLocationRowProvider :
  ColorParamsProvider<PayeeLocationItem>(PayeeLocationsPreview.all)

@Preview
@Composable
private fun PreviewPayeeLocationRow(
  @PreviewParameter(PayeeLocationRowProvider::class) params: ColorParams<PayeeLocationItem>,
) = PreviewWithColorParams(params) { PayeeLocationRow(location = this, onClick = {}) }

@Preview
@Composable
private fun PreviewShimmerPayeeLocationRow(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) = PreviewWithColors(colors) { ShimmerPayeeLocationRow() }

@Preview
@Composable
private fun PreviewSwipeablePayeeLocationRow(
  @PreviewParameter(SwipeableProvider::class) params: ColorParams<Boolean>,
) =
  PreviewWithColorParams(params) {
    SwipeablePayeeLocationRow(
      location = PayeeLocationsPreview.tesco,
      isOpen = this,
      onOpenChange = {},
      onClick = {},
      onDelete = {},
    )
  }

private class SwipeableProvider : ColorParamsProvider<Boolean>(false, true)
