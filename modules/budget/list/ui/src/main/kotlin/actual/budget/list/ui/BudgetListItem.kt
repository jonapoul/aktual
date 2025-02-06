package actual.budget.list.ui

import actual.budget.list.vm.Budget
import actual.budget.list.vm.BudgetState
import actual.core.icons.ActualIcons
import actual.core.icons.Key
import actual.core.res.CoreStrings
import actual.core.ui.ActualFontFamily
import actual.core.ui.BareActualIconButton
import actual.core.ui.HorizontalSpacer
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewActualColumn
import actual.core.ui.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
@Composable
internal fun BudgetListItem(
  budget: Budget,
  onClickOpen: () -> Unit,
  onClickDelete: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier
      .padding(5.dp)
      .background(theme.buttonNormalBackground, RowShape)
      .padding(horizontal = 15.dp, vertical = 12.dp)
      .clickable(onClick = onClickOpen),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
    ) {
      Text(
        text = budget.name,
        fontSize = 16.sp,
        fontFamily = ActualFontFamily,
        fontWeight = FontWeight.W700,
        color = theme.pageText,
      )

      BudgetStateText(
        state = budget.state,
        theme = theme,
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Icon(
        modifier = Modifier.size(13.dp),
        imageVector = ActualIcons.Key,
        contentDescription = budgetDescription(budget),
        tint = if (budget.hasKey) theme.formLabelText else theme.buttonNormalDisabledText,
      )

      HorizontalSpacer(8.dp)

      var showDeleteMenu by remember { mutableStateOf(false) }

      BareActualIconButton(
        imageVector = Icons.Filled.MoreVert,
        contentDescription = CoreStrings.navBack,
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
private fun DeleteMenu(
  expanded: Boolean,
  onDismiss: () -> Unit,
  onClickDelete: () -> Unit,
) {
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismiss,
  ) {
    val deleteText = CoreStrings.budgetDelete
    DropdownMenuItem(
      text = { Text(deleteText, fontFamily = ActualFontFamily) },
      leadingIcon = { Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = deleteText) },
      onClick = onClickDelete,
    )
  }
}

private val RowShape = RoundedCornerShape(size = 6.dp)

@Stable
@Composable
@ReadOnlyComposable
private fun budgetDescription(budget: Budget): String? = if (budget.encryptKeyId != null) {
  if (budget.hasKey) CoreStrings.listBudgetsEncryptedWithKey else CoreStrings.listBudgetsEncryptedWithoutKey
} else {
  null
}

@Preview
@Composable
private fun Synced() = PreviewActualColumn {
  BudgetListItem(
    modifier = Modifier.fillMaxWidth(),
    budget = PreviewBudget,
    onClickOpen = {},
    onClickDelete = {},
  )
}

@Preview
@Composable
private fun Warning() = PreviewActualColumn {
  BudgetListItem(
    modifier = Modifier.fillMaxWidth(),
    budget = PreviewBudget.copy(state = BudgetState.Broken, hasKey = false),
    onClickOpen = {},
    onClickDelete = {},
  )
}
