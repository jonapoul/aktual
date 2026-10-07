package aktual.budget.list.ui

import aktual.budget.model.Budget
import aktual.core.icons.AktualIcons
import aktual.core.icons.Key
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.RowShape
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.contrastingTextColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentListOf

/**
 * actual/packages/desktop-client/src/components/manager/BudgetList.tsx
 *
 * When updating this, make sure to also change [ShimmerBudgetListItem]
 */
@Composable
internal fun BudgetListItem(
  budget: Budget,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onClickOpen: () -> Unit,
  onClickDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val deleteBackground = colors.errorText
  SwipeToReveal(
    actions =
      persistentListOf(
        SwipeAction(
          text = Strings.budgetDelete,
          icon = MaterialIcons.Delete,
          background = deleteBackground,
          foreground = deleteBackground.contrastingTextColor(),
          onClick = {
            onOpenChange(false)
            onClickDelete()
          },
        ),
      ),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    BudgetListItemRow(
      budget = budget,
      onClick = { if (isOpen) onOpenChange(false) else onClickOpen() },
    )
  }
}

@Composable
private fun BudgetListItemRow(
  budget: Budget,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.buttonNormalBackground, RowShape)
        .border(Hairline, colors.pillBorderDark, RowShape)
        .clickable(onClick = onClick)
        .padding(horizontal = 15.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = CenterVertically,
  ) {
    val description = budgetDescription(budget)

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = budget.name,
        fontSize = 16.sp,
        fontWeight = W700,
        color = colors.pageText,
      )

      BudgetStateText(state = budget.state)

      Text(
        modifier = Modifier.padding(top = 4.dp),
        text = description,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
        fontSize = 10.sp,
        lineHeight = 12.sp,
      )
    }

    if (budget.encryptKeyId != null) {
      Icon(
        modifier = Modifier.size(13.dp),
        imageVector = AktualIcons.Key,
        contentDescription = description,
        tint = if (budget.hasKey) colors.formLabelText else colors.buttonNormalDisabledText,
      )
    }
  }
}

@Composable
private fun budgetDescription(budget: Budget) =
  when {
    budget is Unknown -> Strings.listBudgetsOffline
    budget.encryptKeyId == null -> Strings.listBudgetsUnencrypted
    budget.hasKey -> Strings.listBudgetsEncryptedWithKey
    else -> Strings.listBudgetsEncryptedWithoutKey
  }

@Preview
@Composable
private fun PreviewBudgetListItem(
  @PreviewParameter(BudgetListItemProvider::class) params: ColoredParams<BudgetListItemParams>,
) =
  PreviewWithColoredParams(params) {
    BudgetListItem(
      modifier = width?.let { w -> Modifier.width(w) } ?: Modifier.fillMaxWidth(),
      budget = budget,
      isOpen = false,
      onOpenChange = {},
      onClickOpen = {},
      onClickDelete = {},
    )
  }

private data class BudgetListItemParams(val budget: Budget, val width: Dp? = null)

private class BudgetListItemProvider :
  ColoredParameterProvider<BudgetListItemParams>(
    BudgetListItemParams(PreviewBudgetSynced),
    BudgetListItemParams(PreviewBudgetSynced, width = 300.dp),
    BudgetListItemParams(PreviewBudgetBroken),
  )
