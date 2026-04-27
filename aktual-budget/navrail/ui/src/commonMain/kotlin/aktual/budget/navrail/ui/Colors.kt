package aktual.budget.navrail.ui

import aktual.core.theme.BottomBarThemeAttrs

// Shared across BottomNavRail (inside BudgetNavRail.kt) and BudgetNavRailNavEntryContributor,
// so the nav rail's blur color and the root bottom status bar's blur color stay in lockstep
internal val BudgetNavRailThemeAttrs =
  BottomBarThemeAttrs(
    shouldBlurOnRootLevel = false,
    background = { sidebarBackground },
    foreground = { sidebarItemText },
  )
