package aktual.budget.navrail.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.Reports
import aktual.core.icons.Tuning
import aktual.core.icons.material.Info
import aktual.core.icons.material.LinearScale
import aktual.core.icons.material.Logout
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Settings
import aktual.core.icons.material.SwapHoriz
import aktual.core.l10n.Strings
import aktual.core.nav.BudgetTab
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.disabled
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
internal fun NavSheetItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val indicatorColor = if (selected) theme.sidebarItemTextSelected.disabled else Color.Transparent
  val contentColor = if (selected) theme.sidebarItemTextSelected else theme.sidebarItemText
  Column(
    modifier =
      modifier
        .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
        .padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Box(
      modifier =
        Modifier.size(width = 64.dp, height = 32.dp)
          .clip(RoundedCornerShape(percent = 50))
          .background(indicatorColor),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(24.dp),
      )
    }
    Text(text = label, style = AktualTypography.labelSmall, color = contentColor)
  }
}

@Composable
internal fun BudgetTab.label(): String =
  when (this) {
    BudgetTab.Transactions -> Strings.transactionsTitle
    BudgetTab.Reports -> Strings.reportsTitle
    BudgetTab.Schedules -> Strings.listSchedulesTitle
    BudgetTab.Rules -> Strings.rulesTitle
    BudgetTab.SwitchBudget -> Strings.budgetNavMenuSwitchBudget
    BudgetTab.LogOut -> Strings.budgetNavMenuLogOut
    BudgetTab.Settings -> Strings.budgetNavMenuSettings
    BudgetTab.About -> Strings.budgetNavMenuAbout
  }

@Stable
internal fun BudgetTab.icon(): ImageVector =
  when (this) {
    BudgetTab.Transactions -> MaterialIcons.LinearScale
    BudgetTab.Reports -> AktualIcons.Reports
    BudgetTab.Schedules -> AktualIcons.Calendar3
    BudgetTab.Rules -> AktualIcons.Tuning
    BudgetTab.SwitchBudget -> MaterialIcons.SwapHoriz
    BudgetTab.LogOut -> MaterialIcons.Logout
    BudgetTab.Settings -> MaterialIcons.Settings
    BudgetTab.About -> MaterialIcons.Info
  }

// The expanded-grid action shortcuts map to a BudgetNavAction; Tab-category entries do not
internal fun BudgetTab.asNavAction(): BudgetNavAction? =
  when (this) {
    BudgetTab.SwitchBudget -> SwitchFile
    BudgetTab.LogOut -> LogOut
    BudgetTab.Settings -> Settings
    BudgetTab.About -> About
    BudgetTab.Transactions,
    BudgetTab.Reports,
    BudgetTab.Schedules,
    BudgetTab.Rules -> null
  }
