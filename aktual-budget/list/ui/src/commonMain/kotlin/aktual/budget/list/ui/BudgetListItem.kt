package aktual.budget.list.ui

import aktual.budget.model.Budget
import aktual.core.icons.AktualIcons
import aktual.core.icons.Key
import aktual.core.icons.material.DeleteForever
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.l10n.Strings
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.RowShape
import alakazam.compose.HorizontalSpacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * actual/packages/desktop-client/src/components/manager/BudgetList.tsx
 *
 * When updating this, make sure to also change [ShimmerBudgetListItem]
 */
@Composable
internal fun BudgetListItem(
  budget: Budget,
  onClickOpen: () -> Unit,
  onClickDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .clip(RowShape)
        .background(colors.buttonNormalBackground, RowShape)
        .border(Dp.Hairline, colors.pillBorderDark, RowShape)
        .clickable(onClick = onClickOpen)
        .padding(horizontal = 15.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val description = budgetDescription(budget)

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = budget.name,
        fontSize = 16.sp,
        fontWeight = FontWeight.W700,
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

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (budget.encryptKeyId != null) {
        Icon(
          modifier = Modifier.size(13.dp),
          imageVector = AktualIcons.Key,
          contentDescription = description,
          tint = if (budget.hasKey) colors.formLabelText else colors.buttonNormalDisabledText,
        )

        HorizontalSpacer(8.dp)
      }

      var showDeleteMenu by remember { mutableStateOf(false) }

      BareIconButton(
        imageVector = MaterialIcons.MoreVert,
        contentDescription = Strings.navBack,
        onClick = { showDeleteMenu = true },
      )

      DeleteMenu(
        expanded = showDeleteMenu,
        onDismiss = { showDeleteMenu = false },
        onClickDelete = onClickDelete,
      )
    }
  }
}

@Composable
private fun DeleteMenu(expanded: Boolean, onDismiss: () -> Unit, onClickDelete: () -> Unit) {
  AktualDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
    val deleteText = Strings.budgetDelete
    AktualDropdownMenuItem(
      text = { Text(deleteText) },
      leadingIcon = {
        Icon(imageVector = MaterialIcons.DeleteForever, contentDescription = deleteText)
      },
      onClick = {
        onDismiss()
        onClickDelete()
      },
    )
  }
}

@Composable
private fun budgetDescription(budget: Budget) =
  when {
    budget is Budget.Unknown -> Strings.listBudgetsOffline
    budget.encryptKeyId == null -> Strings.listBudgetsUnencrypted
    budget.hasKey -> Strings.listBudgetsEncryptedWithKey
    else -> Strings.listBudgetsEncryptedWithoutKey
  }

@Preview
@Composable
private fun PreviewBudgetListItem(
  @PreviewParameter(BudgetListItemProvider::class) params: ColoredParams<BudgetListItemParams>
) =
  PreviewWithColoredParams(params) {
    BudgetListItem(
      modifier = width?.let { w -> Modifier.width(w) } ?: Modifier.fillMaxWidth(),
      budget = budget,
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
