package aktual.core.theme

import aktual.core.model.ThemeId
import aktual.core.theme.internal.ColorPalette
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data object LightColors : DefaultColors {
  override val id = ThemeId("Light")
  override val isLight = true

  override val pageBackground = ColorPalette.navy100
  override val pageBackgroundModalActive = ColorPalette.navy200
  override val pageBackgroundTopLeft = ColorPalette.navy100
  override val pageBackgroundBottomRight = ColorPalette.blue150
  override val pageBackgroundLineTop = ColorPalette.white
  override val pageBackgroundLineMid = ColorPalette.navy100
  override val pageBackgroundLineBottom = ColorPalette.blue150
  override val pageText = Color(color = 0xFF272630)
  override val pageTextLight = ColorPalette.navy500
  override val pageTextSubdued = ColorPalette.navy300
  override val pageTextDark = ColorPalette.navy800
  override val pageTextPositive = ColorPalette.purple600
  override val pageTextLink = ColorPalette.blue600
  override val pageTextLinkLight = ColorPalette.blue300

  override val cardBackground = ColorPalette.white
  override val cardBorder = ColorPalette.purple700
  override val cardShadow = ColorPalette.navy700

  override val tableBackground = ColorPalette.white
  override val tableRowBackgroundAlternate = tableBackground
  override val tableRowBackgroundHover = ColorPalette.navy50
  override val tableText = pageText
  override val tableTextItemAdded = tableText
  override val tableTextLight = ColorPalette.navy400
  override val tableTextSubdued = ColorPalette.navy100
  override val tableTextSelected = ColorPalette.navy700
  override val tableTextHover = ColorPalette.navy900
  override val tableTextInactive = ColorPalette.navy500
  override val tableHeaderText = ColorPalette.navy600
  override val tableHeaderBackground = ColorPalette.white
  override val tableBorder = ColorPalette.navy100
  override val tableBorderSelected = ColorPalette.purple500
  override val tableBorderHover = ColorPalette.purple400
  override val tableBorderSeparator = ColorPalette.navy400
  override val tableRowBackgroundHighlight = ColorPalette.blue150
  override val tableRowBackgroundHighlightText = ColorPalette.navy700
  override val tableRowHeaderBackground = ColorPalette.navy50
  override val tableRowHeaderText = ColorPalette.navy800

  override val numberPositive = ColorPalette.green700
  override val numberNegative = ColorPalette.red500
  override val numberNeutral = ColorPalette.navy100
  override val budgetNumberNegative = numberNegative
  override val budgetNumberZero = tableTextSubdued
  override val budgetNumberNeutral = tableText
  override val budgetNumberPositive = budgetNumberNeutral
  override val templateNumberFunded = numberPositive
  override val templateNumberUnderFunded = ColorPalette.orange700
  override val toBudgetPositive = numberPositive
  override val toBudgetZero = numberPositive
  override val toBudgetNegative = budgetNumberNegative

  override val sidebarBackground = ColorPalette.navy900
  override val sidebarItemBackgroundPending = ColorPalette.orange200
  override val sidebarItemBackgroundPositive = ColorPalette.green500
  override val sidebarItemBackgroundFailed = ColorPalette.red300
  override val sidebarItemBackgroundHover = ColorPalette.navy800
  override val sidebarItemAccentSelected = ColorPalette.purple200
  override val sidebarItemText = ColorPalette.navy150
  override val sidebarItemTextUpdated = sidebarItemText
  override val sidebarItemTextSelected = ColorPalette.purple200
  override val sidebarBudgetName = ColorPalette.navy150

  override val menuBackground = ColorPalette.white
  override val menuItemBackground = ColorPalette.navy50
  override val menuItemBackgroundHover = ColorPalette.navy100
  override val menuItemText = ColorPalette.navy900
  override val menuItemTextHover = menuItemText
  override val menuItemTextSelected = ColorPalette.purple300
  override val menuItemTextHeader = ColorPalette.navy400
  override val menuBorder = ColorPalette.navy100
  override val menuBorderHover = ColorPalette.purple100
  override val menuKeybindingText = ColorPalette.navy400
  override val menuAutoCompleteBackground = ColorPalette.navy900
  override val menuAutoCompleteBackgroundHover = ColorPalette.navy600
  override val menuAutoCompleteText = ColorPalette.white
  override val menuAutoCompleteTextHover = ColorPalette.green150
  override val menuAutoCompleteTextHeader = ColorPalette.orange150
  override val menuAutoCompleteItemTextHover = menuAutoCompleteText
  override val menuAutoCompleteItemText = menuAutoCompleteText

  override val modalBackground = ColorPalette.white
  override val modalBorder = ColorPalette.white
  override val mobileHeaderBackground = ColorPalette.purple400
  override val mobileHeaderText = ColorPalette.navy50
  override val mobileHeaderTextSubdued = ColorPalette.gray200
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = ColorPalette.navy50
  override val mobileNavBackground = ColorPalette.white
  override val mobileNavItem = ColorPalette.gray300
  override val mobileNavItemSelected = ColorPalette.purple500
  override val mobileAccountShadow = ColorPalette.navy300
  override val mobileAccountText = ColorPalette.blue800
  override val mobileTransactionSelected = ColorPalette.purple500

  // Mobile view themes (for the top bar)
  override val mobileViewTheme = mobileHeaderBackground
  override val mobileConfigServerViewTheme = ColorPalette.purple500

  override val markdownNormal = ColorPalette.purple150
  override val markdownDark = ColorPalette.purple400
  override val markdownLight = ColorPalette.purple100

  // Button
  override val buttonMenuText = ColorPalette.navy100
  override val buttonMenuTextHover = ColorPalette.navy50
  override val buttonMenuBackground = Color.Transparent
  override val buttonMenuBackgroundHover = Color(color = 0x40C8C8C8)
  override val buttonMenuBorder = ColorPalette.navy500
  override val buttonMenuSelectedText = ColorPalette.green800
  override val buttonMenuSelectedTextHover = ColorPalette.orange800
  override val buttonMenuSelectedBackground = ColorPalette.orange200
  override val buttonMenuSelectedBackgroundHover = ColorPalette.orange300
  override val buttonMenuSelectedBorder = buttonMenuSelectedBackground

  override val buttonPrimaryText = ColorPalette.white
  override val buttonPrimaryTextHover = buttonPrimaryText
  override val buttonPrimaryBackground = ColorPalette.purple500
  override val buttonPrimaryBackgroundHover = ColorPalette.purple300
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x4D000000)
  override val buttonPrimaryDisabledText = ColorPalette.white
  override val buttonPrimaryDisabledBackground = ColorPalette.navy300
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = ColorPalette.navy900
  override val buttonNormalTextHover = buttonNormalText
  override val buttonNormalBackground = ColorPalette.white
  override val buttonNormalBackgroundHover = buttonNormalBackground
  override val buttonNormalBorder = ColorPalette.navy150
  override val buttonNormalShadow = Color(color = 0x33000000)
  override val buttonNormalSelectedText = ColorPalette.white
  override val buttonNormalSelectedBackground = ColorPalette.blue600
  override val buttonNormalDisabledText = ColorPalette.navy300
  override val buttonNormalDisabledBackground = buttonNormalBackground
  override val buttonNormalDisabledBorder = buttonNormalBorder

  override val calendarText = ColorPalette.navy50
  override val calendarBackground = ColorPalette.navy900
  override val calendarItemText = ColorPalette.navy150
  override val calendarItemBackground = ColorPalette.navy800
  override val calendarSelectedBackground = ColorPalette.navy500
  override val calendarCellBackground = ColorPalette.navy100

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0xFFE7E1E8)
  override val buttonBareBackgroundActive = Color(color = 0xFFD8D2D8)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = ColorPalette.green150
  override val noticeBackgroundLight = ColorPalette.green100
  override val noticeBackgroundDark = ColorPalette.green500
  override val noticeText = ColorPalette.green700
  override val noticeTextLight = ColorPalette.green500
  override val noticeTextDark = ColorPalette.green900
  override val noticeTextMenu = ColorPalette.green200
  override val noticeTextMenuHover = noticeTextMenu
  override val noticeBorder = ColorPalette.green500
  override val warningBackground = ColorPalette.orange200
  override val warningText = ColorPalette.orange700
  override val warningTextLight = ColorPalette.orange500
  override val warningTextDark = ColorPalette.orange900
  override val warningBorder = ColorPalette.orange500
  override val errorBackground = ColorPalette.red100
  override val errorText = ColorPalette.red500
  override val errorTextDark = ColorPalette.red700
  override val errorTextDarker = ColorPalette.red900
  override val errorTextMenu = ColorPalette.red200
  override val errorBorder = ColorPalette.red500
  override val upcomingBackground = ColorPalette.purple100
  override val upcomingText = ColorPalette.purple700
  override val upcomingBorder = ColorPalette.purple500

  override val formLabelText = ColorPalette.blue600
  override val formLabelBackground = ColorPalette.blue200
  override val formInputBackground = ColorPalette.navy50
  override val formInputBackgroundSelected = ColorPalette.white
  override val formInputBackgroundSelection = ColorPalette.purple500
  override val formInputBorder = ColorPalette.navy150
  override val formInputBorderSelected = ColorPalette.purple500
  override val formInputText = ColorPalette.navy900
  override val formInputTextSelected = ColorPalette.navy50
  override val formInputTextPlaceholder = ColorPalette.navy300
  override val formInputTextPlaceholderSelected = ColorPalette.navy200
  override val formInputTextReadOnlySelection = ColorPalette.navy50
  override val formInputTextSelection = ColorPalette.navy100
  override val formInputShadowSelected = ColorPalette.purple300
  override val formInputTextHighlight = ColorPalette.purple200
  override val checkboxText = tableBackground
  override val checkboxBackgroundSelected = ColorPalette.blue500
  override val checkboxBorderSelected = ColorPalette.blue500
  override val checkboxShadowSelected = ColorPalette.blue300
  override val checkboxToggleBackground = ColorPalette.gray400
  override val checkboxToggleBackgroundSelected = ColorPalette.purple600
  override val checkboxToggleDisabled = ColorPalette.gray200

  override val pillBackground = ColorPalette.navy150
  override val pillBackgroundLight = ColorPalette.navy50
  override val pillText = ColorPalette.navy800
  override val pillTextHighlighted = ColorPalette.purple600
  override val pillBorder = ColorPalette.navy150
  override val pillBorderDark = ColorPalette.navy300
  override val pillBackgroundSelected = ColorPalette.blue150
  override val pillTextSelected = ColorPalette.blue900
  override val pillBorderSelected = ColorPalette.purple500
  override val pillTextSubdued = ColorPalette.navy200

  override val reportsRed = ColorPalette.red300
  override val reportsBlue = ColorPalette.blue400
  override val reportsGreen = ColorPalette.green400
  override val reportsGray = ColorPalette.gray400
  override val reportsLabel = ColorPalette.navy900
  override val reportsInnerLabel = ColorPalette.navy800
  override val reportsNumberPositive = numberPositive
  override val reportsNumberNegative = numberNegative
  override val reportsNumberNeutral = numberNeutral
  override val reportsChartFill = reportsNumberPositive

  override val noteTagBackground = ColorPalette.purple125
  override val noteTagBackgroundHover = ColorPalette.purple150
  override val noteTagDefault = ColorPalette.purple125
  override val noteTagText = ColorPalette.black

  override val budgetCurrentMonth = tableBackground
  override val budgetOtherMonth = ColorPalette.gray50
  override val budgetHeaderCurrentMonth = budgetOtherMonth
  override val budgetHeaderOtherMonth = ColorPalette.gray80

  override val floatingActionBarBackground = ColorPalette.purple400
  override val floatingActionBarBorder = floatingActionBarBackground
  override val floatingActionBarText = ColorPalette.navy50

  override val tooltipText = ColorPalette.navy900
  override val tooltipBackground = ColorPalette.white
  override val tooltipBorder = ColorPalette.navy150

  override val overlayBackground = Color(color = 0x4D000000)

  override val chartQual1 = ColorPalette.chartQual1
  override val chartQual2 = ColorPalette.chartQual2
  override val chartQual3 = ColorPalette.chartQual3
  override val chartQual4 = ColorPalette.chartQual4
  override val chartQual5 = ColorPalette.chartQual5
  override val chartQual6 = ColorPalette.chartQual6
  override val chartQual7 = ColorPalette.chartQual7
  override val chartQual8 = ColorPalette.chartQual8
  override val chartQual9 = ColorPalette.chartQual9
}
