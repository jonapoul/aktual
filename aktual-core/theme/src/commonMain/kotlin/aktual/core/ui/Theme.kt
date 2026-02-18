package aktual.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
@Suppress("ComplexInterface")
interface Theme {
  val pageBackground: Color
  val pageBackgroundModalActive: Color
  val pageBackgroundTopLeft: Color
  val pageBackgroundBottomRight: Color
  val pageBackgroundLineTop: Color
  val pageBackgroundLineMid: Color
  val pageBackgroundLineBottom: Color
  val pageText: Color
  val pageTextLight: Color
  val pageTextSubdued: Color
  val pageTextDark: Color
  val pageTextPositive: Color
  val pageTextLink: Color
  val pageTextLinkLight: Color
  val numberPositive: Color
  val numberNegative: Color
  val numberNeutral: Color

  val cardBackground: Color
  val cardBorder: Color
  val cardShadow: Color

  val tableBackground: Color
  val tableRowBackgroundHover: Color
  val tableText: Color
  val tableTextLight: Color
  val tableTextSubdued: Color
  val tableTextSelected: Color
  val tableTextHover: Color
  val tableTextInactive: Color
  val tableHeaderText: Color
  val tableHeaderBackground: Color
  val tableBorder: Color
  val tableBorderSelected: Color
  val tableBorderHover: Color
  val tableBorderSeparator: Color
  val tableRowBackgroundHighlight: Color
  val tableRowBackgroundHighlightText: Color
  val tableRowHeaderBackground: Color
  val tableRowHeaderText: Color

  val sidebarBackground: Color
  val sidebarItemBackgroundPending: Color
  val sidebarItemBackgroundPositive: Color
  val sidebarItemBackgroundFailed: Color
  val sidebarItemBackgroundHover: Color
  val sidebarItemAccentSelected: Color
  val sidebarItemText: Color
  val sidebarItemTextSelected: Color
  val sidebarBudgetName: Color

  val menuBackground: Color
  val menuItemBackground: Color
  val menuItemBackgroundHover: Color
  val menuItemText: Color
  val menuItemTextHover: Color
  val menuItemTextSelected: Color
  val menuItemTextHeader: Color
  val menuBorder: Color
  val menuBorderHover: Color
  val menuKeybindingText: Color
  val menuAutoCompleteBackground: Color
  val menuAutoCompleteBackgroundHover: Color
  val menuAutoCompleteText: Color
  val menuAutoCompleteTextHover: Color
  val menuAutoCompleteTextHeader: Color
  val menuAutoCompleteItemTextHover: Color
  val menuAutoCompleteItemText: Color

  val modalBackground: Color
  val modalBorder: Color
  val mobileHeaderBackground: Color
  val mobileHeaderText: Color
  val mobileHeaderTextSubdued: Color
  val mobileHeaderTextHover: Color
  val mobilePageBackground: Color
  val mobileNavBackground: Color
  val mobileNavItem: Color
  val mobileNavItemSelected: Color
  val mobileAccountShadow: Color
  val mobileAccountText: Color
  val mobileTransactionSelected: Color

  // Mobile view themes (for the top bar)
  val mobileViewTheme: Color
  val mobileConfigServerViewTheme: Color

  val markdownNormal: Color
  val markdownDark: Color
  val markdownLight: Color

  // Button
  val buttonMenuText: Color
  val buttonMenuTextHover: Color
  val buttonMenuBackground: Color
  val buttonMenuBackgroundHover: Color
  val buttonMenuBorder: Color
  val buttonMenuSelectedText: Color
  val buttonMenuSelectedTextHover: Color
  val buttonMenuSelectedBackground: Color
  val buttonMenuSelectedBackgroundHover: Color
  val buttonMenuSelectedBorder: Color

  val buttonPrimaryText: Color
  val buttonPrimaryTextHover: Color
  val buttonPrimaryBackground: Color
  val buttonPrimaryBackgroundHover: Color
  val buttonPrimaryBorder: Color
  val buttonPrimaryShadow: Color
  val buttonPrimaryDisabledText: Color
  val buttonPrimaryDisabledBackground: Color
  val buttonPrimaryDisabledBorder: Color

  val buttonNormalText: Color
  val buttonNormalTextHover: Color
  val buttonNormalBackground: Color
  val buttonNormalBackgroundHover: Color
  val buttonNormalBorder: Color
  val buttonNormalShadow: Color
  val buttonNormalSelectedText: Color
  val buttonNormalSelectedBackground: Color
  val buttonNormalDisabledText: Color
  val buttonNormalDisabledBackground: Color
  val buttonNormalDisabledBorder: Color

  val calendarText: Color
  val calendarBackground: Color
  val calendarItemText: Color
  val calendarItemBackground: Color
  val calendarSelectedBackground: Color
  val calendarCellBackground: Color

  val buttonBareText: Color
  val buttonBareTextHover: Color
  val buttonBareBackground: Color
  val buttonBareBackgroundHover: Color
  val buttonBareBackgroundActive: Color
  val buttonBareDisabledText: Color
  val buttonBareDisabledBackground: Color

  val noticeBackground: Color
  val noticeBackgroundLight: Color
  val noticeBackgroundDark: Color
  val noticeText: Color
  val noticeTextLight: Color
  val noticeTextDark: Color
  val noticeTextMenu: Color
  val noticeBorder: Color
  val warningBackground: Color
  val warningText: Color
  val warningTextLight: Color
  val warningTextDark: Color
  val warningBorder: Color
  val errorBackground: Color
  val errorText: Color
  val errorTextDark: Color
  val errorTextDarker: Color
  val errorTextMenu: Color
  val errorBorder: Color
  val upcomingBackground: Color
  val upcomingText: Color
  val upcomingBorder: Color

  val formLabelText: Color
  val formLabelBackground: Color
  val formInputBackground: Color
  val formInputBackgroundSelected: Color
  val formInputBackgroundSelection: Color
  val formInputBorder: Color
  val formInputBorderSelected: Color
  val formInputText: Color
  val formInputTextSelected: Color
  val formInputTextPlaceholder: Color
  val formInputTextPlaceholderSelected: Color
  val formInputTextReadOnlySelection: Color
  val formInputTextSelection: Color
  val formInputShadowSelected: Color
  val formInputTextHighlight: Color
  val checkboxText: Color
  val checkboxBackgroundSelected: Color
  val checkboxBorderSelected: Color
  val checkboxShadowSelected: Color
  val checkboxToggleBackground: Color
  val checkboxToggleBackgroundSelected: Color
  val checkboxToggleDisabled: Color

  val pillBackground: Color
  val pillBackgroundLight: Color
  val pillText: Color
  val pillTextHighlighted: Color
  val pillBorder: Color
  val pillBorderDark: Color
  val pillBackgroundSelected: Color
  val pillTextSelected: Color
  val pillBorderSelected: Color
  val pillTextSubdued: Color

  val reportsRed: Color
  val reportsBlue: Color
  val reportsGreen: Color
  val reportsGray: Color
  val reportsLabel: Color
  val reportsInnerLabel: Color
  val reportsChartFill: Color
  val reportsNumberPositive: Color
  val reportsNumberNegative: Color
  val reportsNumberNeutral: Color

  val noteTagBackground: Color
  val noteTagBackgroundHover: Color
  val noteTagDefault: Color
  val noteTagText: Color

  val budgetCurrentMonth: Color
  val budgetOtherMonth: Color
  val budgetHeaderCurrentMonth: Color
  val budgetHeaderOtherMonth: Color
  val budgetNumberZero: Color
  val budgetNumberNegative: Color
  val budgetNumberNeutral: Color
  val budgetNumberPositive: Color
  val templateNumberFunded: Color
  val templateNumberUnderFunded: Color
  val toBudgetPositive: Color
  val toBudgetZero: Color
  val toBudgetNegative: Color

  val floatingActionBarBackground: Color
  val floatingActionBarBorder: Color
  val floatingActionBarText: Color

  val tooltipText: Color
  val tooltipBackground: Color
  val tooltipBorder: Color

  val overlayBackground: Color

  companion object {
    fun light(): Theme = LightTheme()

    fun dark(): Theme = DarkTheme()

    fun midnight(): Theme = MidnightTheme()
  }
}
