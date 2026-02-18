package aktual.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal class DarkTheme : Theme {
  override val pageBackground = AktualColors.gray900
  override val pageBackgroundModalActive = AktualColors.gray800
  override val pageBackgroundTopLeft = AktualColors.navy800
  override val pageBackgroundBottomRight = AktualColors.gray700
  override val pageBackgroundLineTop = AktualColors.purple400
  override val pageBackgroundLineMid = AktualColors.navy900
  override val pageBackgroundLineBottom = AktualColors.navy150
  override val pageText = AktualColors.navy150
  override val pageTextLight = AktualColors.navy300
  override val pageTextSubdued = AktualColors.navy500
  override val pageTextDark = AktualColors.navy100
  override val pageTextPositive = AktualColors.purple200
  override val pageTextLink = AktualColors.purple400
  override val pageTextLinkLight = AktualColors.purple200
  override val numberPositive = AktualColors.green300
  override val numberNegative = AktualColors.red200
  override val numberNeutral = AktualColors.navy500

  override val cardBackground = AktualColors.gray800
  override val cardBorder = AktualColors.purple400
  override val cardShadow = AktualColors.navy700

  override val tableBackground = AktualColors.navy800
  override val tableRowBackgroundHover = AktualColors.navy700
  override val tableText = AktualColors.navy150
  override val tableTextLight = tableText
  override val tableTextSubdued = AktualColors.navy500
  override val tableTextSelected = AktualColors.navy150
  override val tableTextHover = AktualColors.navy400
  override val tableTextInactive = AktualColors.navy500
  override val tableHeaderText = AktualColors.navy300
  override val tableHeaderBackground = AktualColors.navy700
  override val tableBorder = AktualColors.navy600
  override val tableBorderSelected = AktualColors.purple400
  override val tableBorderHover = AktualColors.purple300
  override val tableBorderSeparator = AktualColors.navy400
  override val tableRowBackgroundHighlight = AktualColors.purple800
  override val tableRowBackgroundHighlightText = AktualColors.navy150
  override val tableRowHeaderBackground = AktualColors.navy700
  override val tableRowHeaderText = AktualColors.navy150

  override val sidebarBackground = AktualColors.navy900
  override val sidebarItemBackgroundPending = AktualColors.orange200
  override val sidebarItemBackgroundPositive = AktualColors.green500
  override val sidebarItemBackgroundFailed = AktualColors.red300
  override val sidebarItemAccentSelected = AktualColors.purple200
  override val sidebarItemBackgroundHover = AktualColors.navy700
  override val sidebarItemText = AktualColors.navy150
  override val sidebarItemTextSelected = AktualColors.purple200
  override val sidebarBudgetName = AktualColors.navy300

  override val menuBackground = AktualColors.navy800
  override val menuItemBackground = AktualColors.navy800
  override val menuItemBackgroundHover = AktualColors.navy500
  override val menuItemText = AktualColors.navy100
  override val menuItemTextHover = AktualColors.navy50
  override val menuItemTextSelected = AktualColors.purple400
  override val menuItemTextHeader = AktualColors.purple200
  override val menuBorder = AktualColors.navy900
  override val menuBorderHover = AktualColors.purple400
  override val menuKeybindingText = AktualColors.purple200
  override val menuAutoCompleteBackground = AktualColors.navy900
  override val menuAutoCompleteBackgroundHover = AktualColors.navy600
  override val menuAutoCompleteText = AktualColors.navy200
  override val menuAutoCompleteTextHover = AktualColors.green150
  override val menuAutoCompleteTextHeader = AktualColors.purple200
  override val menuAutoCompleteItemTextHover = AktualColors.navy50
  override val menuAutoCompleteItemText = menuItemText

  override val modalBackground = AktualColors.gray800
  override val modalBorder = AktualColors.navy600
  override val mobileHeaderBackground = AktualColors.purple800
  override val mobileHeaderText = AktualColors.navy150
  override val mobileHeaderTextSubdued = AktualColors.gray200
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = AktualColors.navy700
  override val mobileNavBackground = AktualColors.navy800
  override val mobileNavItem = AktualColors.navy150
  override val mobileNavItemSelected = AktualColors.purple400
  override val mobileAccountShadow = cardShadow
  override val mobileAccountText = AktualColors.blue800
  override val mobileTransactionSelected = AktualColors.purple400

  // Mobile view themes (for the top bar)
  override val mobileViewTheme = mobileHeaderBackground
  override val mobileConfigServerViewTheme = AktualColors.purple500

  override val markdownNormal = AktualColors.purple700
  override val markdownDark = AktualColors.purple500
  override val markdownLight = AktualColors.purple800

  // Button
  override val buttonMenuText = AktualColors.navy200
  override val buttonMenuTextHover = buttonMenuText
  override val buttonMenuBackground = Color.Transparent
  override val buttonMenuBackgroundHover = Color(color = 0x40C8C8C8)
  override val buttonMenuBorder = AktualColors.navy500
  override val buttonMenuSelectedText = AktualColors.green800
  override val buttonMenuSelectedTextHover = AktualColors.orange800
  override val buttonMenuSelectedBackground = AktualColors.orange200
  override val buttonMenuSelectedBackgroundHover = AktualColors.orange300
  override val buttonMenuSelectedBorder = buttonMenuSelectedBackground

  override val buttonPrimaryText = AktualColors.white
  override val buttonPrimaryTextHover = buttonPrimaryText
  override val buttonPrimaryBackground = AktualColors.purple400
  override val buttonPrimaryBackgroundHover = AktualColors.purple600
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x99000000)
  override val buttonPrimaryDisabledText = AktualColors.navy700
  override val buttonPrimaryDisabledBackground = AktualColors.navy400
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = AktualColors.navy150
  override val buttonNormalTextHover = AktualColors.navy150
  override val buttonNormalBackground = AktualColors.navy800
  override val buttonNormalBackgroundHover = AktualColors.navy600
  override val buttonNormalBorder = AktualColors.navy300
  override val buttonNormalShadow = Color(color = 0x66000000)
  override val buttonNormalSelectedText = AktualColors.white
  override val buttonNormalSelectedBackground = AktualColors.purple600
  override val buttonNormalDisabledText = AktualColors.navy500
  override val buttonNormalDisabledBackground = AktualColors.navy800
  override val buttonNormalDisabledBorder = AktualColors.navy500

  override val calendarText = AktualColors.navy50
  override val calendarBackground = AktualColors.navy900
  override val calendarItemText = AktualColors.navy150
  override val calendarItemBackground = AktualColors.navy800
  override val calendarSelectedBackground = buttonNormalSelectedBackground
  override val calendarCellBackground = AktualColors.navy900

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0x4DC8C8C8)
  override val buttonBareBackgroundActive = Color(color = 0x80C8C8C8)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = AktualColors.green800
  override val noticeBackgroundLight = AktualColors.green900
  override val noticeBackgroundDark = AktualColors.green500
  override val noticeText = AktualColors.green300
  override val noticeTextLight = AktualColors.green500
  override val noticeTextDark = AktualColors.green150
  override val noticeTextMenu = AktualColors.green500
  override val noticeBorder = AktualColors.green800
  override val warningBackground = AktualColors.orange800
  override val warningText = AktualColors.orange300
  override val warningTextLight = AktualColors.orange500
  override val warningTextDark = AktualColors.orange100
  override val warningBorder = AktualColors.orange500
  override val errorBackground = AktualColors.red800
  override val errorText = AktualColors.red200
  override val errorTextDark = AktualColors.red150
  override val errorTextDarker = errorTextDark
  override val errorTextMenu = AktualColors.red200
  override val errorBorder = AktualColors.red500
  override val upcomingBackground = AktualColors.purple700
  override val upcomingText = AktualColors.purple100
  override val upcomingBorder = tableBorder

  override val formLabelText = AktualColors.purple150
  override val formLabelBackground = AktualColors.blue900
  override val formInputBackground = AktualColors.navy800
  override val formInputBackgroundSelected = AktualColors.navy700
  override val formInputBackgroundSelection = AktualColors.purple400
  override val formInputBorder = AktualColors.navy600
  override val formInputTextReadOnlySelection = AktualColors.navy800
  override val formInputBorderSelected = AktualColors.purple400
  override val formInputText = AktualColors.navy150
  override val formInputTextSelected = AktualColors.black
  override val formInputTextPlaceholder = AktualColors.navy150
  override val formInputTextPlaceholderSelected = AktualColors.navy100
  override val formInputTextSelection = AktualColors.navy800
  override val formInputShadowSelected = AktualColors.purple200
  override val formInputTextHighlight = AktualColors.purple400
  override val checkboxText = tableText
  override val checkboxBackgroundSelected = AktualColors.purple300
  override val checkboxBorderSelected = AktualColors.purple300
  override val checkboxShadowSelected = AktualColors.purple500
  override val checkboxToggleBackground = AktualColors.gray700
  override val checkboxToggleBackgroundSelected = AktualColors.purple300
  override val checkboxToggleDisabled = AktualColors.gray400

  override val pillBackground = AktualColors.navy800
  override val pillBackgroundLight = AktualColors.navy900
  override val pillText = AktualColors.navy200
  override val pillTextHighlighted = AktualColors.purple200
  override val pillBorder = AktualColors.navy700
  override val pillBorderDark = pillBorder
  override val pillBackgroundSelected = AktualColors.purple600
  override val pillTextSelected = AktualColors.navy150
  override val pillBorderSelected = AktualColors.purple400
  override val pillTextSubdued = AktualColors.navy500

  override val reportsRed = AktualColors.red300
  override val reportsBlue = AktualColors.blue400
  override val reportsGreen = AktualColors.green400
  override val reportsGray = AktualColors.gray400
  override val reportsLabel = pageText
  override val reportsInnerLabel = AktualColors.navy800
  override val reportsNumberPositive = numberPositive
  override val reportsNumberNegative = numberNegative
  override val reportsNumberNeutral = numberNeutral
  override val reportsChartFill = reportsNumberPositive

  override val noteTagBackground = AktualColors.purple700
  override val noteTagBackgroundHover = AktualColors.purple500
  override val noteTagDefault = AktualColors.purple700
  override val noteTagText = AktualColors.purple100

  override val budgetCurrentMonth = tableBackground
  override val budgetOtherMonth = AktualColors.navy900
  override val budgetHeaderCurrentMonth = tableHeaderBackground
  override val budgetHeaderOtherMonth = AktualColors.navy800
  override val budgetNumberZero = tableTextSubdued
  override val budgetNumberNegative = numberNegative
  override val budgetNumberNeutral = tableText
  override val budgetNumberPositive = budgetNumberNeutral
  override val templateNumberFunded = numberPositive
  override val templateNumberUnderFunded = AktualColors.orange300
  override val toBudgetPositive = numberPositive
  override val toBudgetZero = numberPositive
  override val toBudgetNegative = budgetNumberNegative

  override val floatingActionBarBackground = AktualColors.purple800
  override val floatingActionBarBorder = floatingActionBarBackground
  override val floatingActionBarText = AktualColors.navy150

  override val tooltipText = AktualColors.navy100
  override val tooltipBackground = AktualColors.navy800
  override val tooltipBorder = AktualColors.navy700

  override val overlayBackground = Color(color = 0x4D000000)
}
