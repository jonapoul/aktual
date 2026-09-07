package aktual.budget.navrail.ui

import aktual.core.theme.BottomBarThemeAttrs

// Shared across BottomNavRail (inside BudgetNavRail.kt) and BudgetNavRailNavEntryContributor,
// so the nav rail's haze color and the root bottom status bar's haze color stay in lockstep
internal val BudgetNavRailThemeAttrs =
  BottomBarThemeAttrs(
    shouldHazeOnRootLevel = false,
    background = { sidebarBackground },
    foreground = { sidebarItemText },
  )
