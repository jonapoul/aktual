package aktual.budget.payees.ui.list

import aktual.budget.payees.vm.list.PayeeItem
import aktual.core.icons.AktualIcons
import aktual.core.icons.Bookmark
import aktual.core.icons.Merge
import aktual.core.icons.Trash
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.RowShape
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.contrastingTextColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf

// Transfer payees can't be deleted, favourited or merged, as upstream, so they don't swipe
@Composable
internal fun SwipeablePayeeItem(
  payee: PayeeItem,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ListPayeesActionHandler,
  modifier: Modifier = Modifier,
  showLearningOff: Boolean = false,
) {
  if (payee.isTransfer) {
    PayeeItem(
      modifier = modifier,
      payee = payee,
      onClick = { onAction(OpenPayee(payee.id)) },
      onClickRules = { onAction(OpenRules(payee)) },
      showLearningOff = showLearningOff,
    )
    return
  }

  val deleteBackground = colors.errorText
  SwipeToReveal(
    actions =
      persistentListOf(
        SwipeAction(
          text = if (payee.isFavorite) Strings.payeesUnfavorite else Strings.payeesFavorite,
          icon = AktualIcons.Bookmark,
          background = lerp(colors.tableBackground, Black, fraction = 0.1f),
          foreground = colors.tableText,
          onClick = {
            onOpenChange(false)
            onAction(ToggleFavorite(payee))
          },
        ),
        SwipeAction(
          text = Strings.payeesMerge,
          icon = AktualIcons.Merge,
          background = lerp(colors.tableBackground, Black, fraction = 0.2f),
          foreground = colors.tableText,
          onClick = {
            onOpenChange(false)
            onAction(StartMerge(payee))
          },
        ),
        SwipeAction(
          text = Strings.payeesDelete,
          icon = AktualIcons.Trash,
          background = deleteBackground,
          foreground = deleteBackground.contrastingTextColor(),
          onClick = {
            onOpenChange(false)
            onAction(Delete(payee))
          },
        ),
      ),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    PayeeItem(
      payee = payee,
      onClick = { onAction(OpenPayee(payee.id)) },
      onClickRules = { onAction(OpenRules(payee)) },
      showLearningOff = showLearningOff,
    )
  }
}

@Preview
@Composable
private fun PreviewSwipeablePayeeItem(
  @PreviewParameter(SwipeableProvider::class) params: ColoredParams<Boolean>,
) =
  PreviewWithColoredParams(params) {
    SwipeablePayeeItem(
      payee = PayeesPreview.tesco,
      isOpen = this,
      onOpenChange = {},
      onAction = {},
    )
  }

private class SwipeableProvider : ColoredParameterProvider<Boolean>(false, true)
