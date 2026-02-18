package aktual.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal class LightTheme : Theme {
  override val pageBackground = AktualColors.navy100
  override val pageBackgroundModalActive = AktualColors.navy200
  override val pageBackgroundTopLeft = AktualColors.navy100
  override val pageBackgroundBottomRight = AktualColors.blue150
  override val pageBackgroundLineTop = AktualColors.white
  override val pageBackgroundLineMid = AktualColors.navy100
  override val pageBackgroundLineBottom = AktualColors.blue150
  override val pageText = Color(color = 0xFF272630)
  override val pageTextLight = AktualColors.navy500
  override val pageTextSubdued = AktualColors.navy300
  override val pageTextDark = AktualColors.navy800
  override val pageTextPositive = AktualColors.purple600
  override val pageTextLink = AktualColors.blue600
  override val pageTextLinkLight = AktualColors.blue300
  override val numberPositive = AktualColors.green700
  override val numberNegative = AktualColors.red500
  override val numberNeutral = AktualColors.navy100

  override val cardBackground = AktualColors.white
  override val cardBorder = AktualColors.purple700
  override val cardShadow = AktualColors.navy700

  override val tableBackground = AktualColors.white
  override val tableRowBackgroundHover = AktualColors.navy50
  override val tableText = pageText
  override val tableTextLight = AktualColors.navy400
  override val tableTextSubdued = AktualColors.navy100
  override val tableTextSelected = AktualColors.navy700
  override val tableTextHover = AktualColors.navy900
  override val tableTextInactive = AktualColors.navy500
  override val tableHeaderText = AktualColors.navy600
  override val tableHeaderBackground = AktualColors.white
  override val tableBorder = AktualColors.navy100
  override val tableBorderSelected = AktualColors.purple500
  override val tableBorderHover = AktualColors.purple400
  override val tableBorderSeparator = AktualColors.navy400
  override val tableRowBackgroundHighlight = AktualColors.blue150
  override val tableRowBackgroundHighlightText = AktualColors.navy700
  override val tableRowHeaderBackground = AktualColors.navy50
  override val tableRowHeaderText = AktualColors.navy800

  override val sidebarBackground = AktualColors.navy900
  override val sidebarItemBackgroundPending = AktualColors.orange200
  override val sidebarItemBackgroundPositive = AktualColors.green500
  override val sidebarItemBackgroundFailed = AktualColors.red300
  override val sidebarItemBackgroundHover = AktualColors.navy800
  override val sidebarItemAccentSelected = AktualColors.purple200
  override val sidebarItemText = AktualColors.navy150
  override val sidebarItemTextSelected = AktualColors.purple200
  override val sidebarBudgetName = AktualColors.navy150

  override val menuBackground = AktualColors.white
  override val menuItemBackground = AktualColors.navy50
  override val menuItemBackgroundHover = AktualColors.navy100
  override val menuItemText = AktualColors.navy900
  override val menuItemTextHover = menuItemText
  override val menuItemTextSelected = AktualColors.purple300
  override val menuItemTextHeader = AktualColors.navy400
  override val menuBorder = AktualColors.navy100
  override val menuBorderHover = AktualColors.purple100
  override val menuKeybindingText = AktualColors.navy400
  override val menuAutoCompleteBackground = AktualColors.navy900
  override val menuAutoCompleteBackgroundHover = AktualColors.navy600
  override val menuAutoCompleteText = AktualColors.white
  override val menuAutoCompleteTextHover = AktualColors.green150
  override val menuAutoCompleteTextHeader = AktualColors.orange150
  override val menuAutoCompleteItemTextHover = menuAutoCompleteText
  override val menuAutoCompleteItemText = menuAutoCompleteText

  override val modalBackground = AktualColors.white
  override val modalBorder = AktualColors.white
  override val mobileHeaderBackground = AktualColors.purple400
  override val mobileHeaderText = AktualColors.navy50
  override val mobileHeaderTextSubdued = AktualColors.gray200
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = AktualColors.navy50
  override val mobileNavBackground = AktualColors.white
  override val mobileNavItem = AktualColors.gray300
  override val mobileNavItemSelected = AktualColors.purple500
  override val mobileAccountShadow = AktualColors.navy300
  override val mobileAccountText = AktualColors.blue800
  override val mobileTransactionSelected = AktualColors.purple500

  // Mobile view themes (for the top bar)
  override val mobileViewTheme = mobileHeaderBackground
  override val mobileConfigServerViewTheme = AktualColors.purple500

  override val markdownNormal = AktualColors.purple150
  override val markdownDark = AktualColors.purple400
  override val markdownLight = AktualColors.purple100

  // Button
  override val buttonMenuText = AktualColors.navy100
  override val buttonMenuTextHover = AktualColors.navy50
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
  override val buttonPrimaryBackground = AktualColors.purple500
  override val buttonPrimaryBackgroundHover = AktualColors.purple300
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x4D000000)
  override val buttonPrimaryDisabledText = AktualColors.white
  override val buttonPrimaryDisabledBackground = AktualColors.navy300
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = AktualColors.navy900
  override val buttonNormalTextHover = buttonNormalText
  override val buttonNormalBackground = AktualColors.white
  override val buttonNormalBackgroundHover = buttonNormalBackground
  override val buttonNormalBorder = AktualColors.navy150
  override val buttonNormalShadow = Color(color = 0x33000000)
  override val buttonNormalSelectedText = AktualColors.white
  override val buttonNormalSelectedBackground = AktualColors.blue600
  override val buttonNormalDisabledText = AktualColors.navy300
  override val buttonNormalDisabledBackground = buttonNormalBackground
  override val buttonNormalDisabledBorder = buttonNormalBorder

  override val calendarText = AktualColors.navy50
  override val calendarBackground = AktualColors.navy900
  override val calendarItemText = AktualColors.navy150
  override val calendarItemBackground = AktualColors.navy800
  override val calendarSelectedBackground = AktualColors.navy500
  override val calendarCellBackground = AktualColors.navy100

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0xFFE7E1E8)
  override val buttonBareBackgroundActive = Color(color = 0xFFD8D2D8)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = AktualColors.green150
  override val noticeBackgroundLight = AktualColors.green100
  override val noticeBackgroundDark = AktualColors.green500
  override val noticeText = AktualColors.green700
  override val noticeTextLight = AktualColors.green500
  override val noticeTextDark = AktualColors.green900
  override val noticeTextMenu = AktualColors.green200
  override val noticeBorder = AktualColors.green500
  override val warningBackground = AktualColors.orange200
  override val warningText = AktualColors.orange700
  override val warningTextLight = AktualColors.orange500
  override val warningTextDark = AktualColors.orange900
  override val warningBorder = AktualColors.orange500
  override val errorBackground = AktualColors.red100
  override val errorText = AktualColors.red500
  override val errorTextDark = AktualColors.red700
  override val errorTextDarker = AktualColors.red900
  override val errorTextMenu = AktualColors.red200
  override val errorBorder = AktualColors.red500
  override val upcomingBackground = AktualColors.purple100
  override val upcomingText = AktualColors.purple700
  override val upcomingBorder = AktualColors.purple500

  override val formLabelText = AktualColors.blue600
  override val formLabelBackground = AktualColors.blue200
  override val formInputBackground = AktualColors.navy50
  override val formInputBackgroundSelected = AktualColors.white
  override val formInputBackgroundSelection = AktualColors.purple500
  override val formInputBorder = AktualColors.navy150
  override val formInputTextReadOnlySelection = AktualColors.navy50
  override val formInputBorderSelected = AktualColors.purple500
  override val formInputText = AktualColors.navy900
  override val formInputTextSelected = AktualColors.navy50
  override val formInputTextPlaceholder = AktualColors.navy300
  override val formInputTextPlaceholderSelected = AktualColors.navy200
  override val formInputTextSelection = AktualColors.navy100
  override val formInputShadowSelected = AktualColors.purple300
  override val formInputTextHighlight = AktualColors.purple200
  override val checkboxText = tableBackground
  override val checkboxBackgroundSelected = AktualColors.blue500
  override val checkboxBorderSelected = AktualColors.blue500
  override val checkboxShadowSelected = AktualColors.blue300
  override val checkboxToggleBackground = AktualColors.gray400
  override val checkboxToggleBackgroundSelected = AktualColors.purple600
  override val checkboxToggleDisabled = AktualColors.gray200

  override val pillBackground = AktualColors.navy150
  override val pillBackgroundLight = AktualColors.navy50
  override val pillText = AktualColors.navy800
  override val pillTextHighlighted = AktualColors.purple600
  override val pillBorder = AktualColors.navy150
  override val pillBorderDark = AktualColors.navy300
  override val pillBackgroundSelected = AktualColors.blue150
  override val pillTextSelected = AktualColors.blue900
  override val pillBorderSelected = AktualColors.purple500
  override val pillTextSubdued = AktualColors.navy200

  override val reportsRed = AktualColors.red300
  override val reportsBlue = AktualColors.blue400
  override val reportsGreen = AktualColors.green400
  override val reportsGray = AktualColors.gray400
  override val reportsLabel = AktualColors.navy900
  override val reportsInnerLabel = AktualColors.navy800
  override val reportsNumberPositive = numberPositive
  override val reportsNumberNegative = numberNegative
  override val reportsNumberNeutral = numberNeutral
  override val reportsChartFill = reportsNumberPositive

  override val noteTagBackground = AktualColors.purple125
  override val noteTagBackgroundHover = AktualColors.purple150
  override val noteTagDefault = AktualColors.purple125
  override val noteTagText = AktualColors.black

  override val budgetCurrentMonth = tableBackground
  override val budgetOtherMonth = AktualColors.gray50
  override val budgetHeaderCurrentMonth = budgetOtherMonth
  override val budgetHeaderOtherMonth = AktualColors.gray80
  override val budgetNumberZero = tableTextSubdued
  override val budgetNumberNegative = numberNegative
  override val budgetNumberNeutral = tableText
  override val budgetNumberPositive = budgetNumberNeutral
  override val templateNumberFunded = numberPositive
  override val templateNumberUnderFunded = AktualColors.orange700
  override val toBudgetPositive = numberPositive
  override val toBudgetZero = numberPositive
  override val toBudgetNegative = budgetNumberNegative

  override val floatingActionBarBackground = AktualColors.purple400
  override val floatingActionBarBorder = floatingActionBarBackground
  override val floatingActionBarText = AktualColors.navy50

  override val tooltipText = AktualColors.navy900
  override val tooltipBackground = AktualColors.white
  override val tooltipBorder = AktualColors.navy150

  override val overlayBackground = Color(color = 0x4D000000)
}
