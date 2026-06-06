package aktual.core.theme

import aktual.core.model.ThemeId
import aktual.core.theme.internal.ColorPalette
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data object MidnightTheme : DefaultTheme {
  override val id = ThemeId("Midnight")
  override val isLight = false

  override val pageBackground = ColorPalette.gray600
  override val pageBackgroundModalActive = ColorPalette.gray700
  override val pageBackgroundTopLeft = ColorPalette.gray800
  override val pageBackgroundBottomRight = ColorPalette.gray700
  override val pageBackgroundLineTop = ColorPalette.purple300
  override val pageBackgroundLineMid = ColorPalette.gray900
  override val pageBackgroundLineBottom = ColorPalette.gray150
  override val pageText = ColorPalette.gray100
  override val pageTextLight = ColorPalette.gray200
  override val pageTextSubdued = ColorPalette.gray400
  override val pageTextDark = ColorPalette.gray100
  override val pageTextPositive = ColorPalette.purple200
  override val pageTextLink = ColorPalette.purple300
  override val pageTextLinkLight = ColorPalette.purple300

  override val cardBackground = ColorPalette.gray800
  override val cardBorder = ColorPalette.purple300
  override val cardShadow = ColorPalette.gray900

  override val tableBackground = ColorPalette.gray800
  override val tableRowBackgroundAlternate = tableBackground
  override val tableRowBackgroundHover = ColorPalette.gray500
  override val tableText = ColorPalette.gray150
  override val tableTextItemAdded = tableText
  override val tableTextLight = tableText
  override val tableTextSubdued = ColorPalette.gray500
  override val tableTextSelected = ColorPalette.gray800
  override val tableTextHover = ColorPalette.gray400
  override val tableTextInactive = ColorPalette.gray400
  override val tableHeaderText = ColorPalette.gray200
  override val tableHeaderBackground = ColorPalette.gray900
  override val tableBorder = ColorPalette.gray600
  override val tableBorderSelected = ColorPalette.purple400
  override val tableBorderHover = ColorPalette.purple300
  override val tableBorderSeparator = ColorPalette.gray400
  override val tableRowBackgroundHighlight = ColorPalette.purple150
  override val tableRowBackgroundHighlightText = ColorPalette.gray800
  override val tableRowHeaderBackground = ColorPalette.gray700
  override val tableRowHeaderText = ColorPalette.gray150

  override val numberPositive = ColorPalette.green300
  override val numberNegative = ColorPalette.red200
  override val numberNeutral = ColorPalette.gray500
  override val budgetNumberNegative = numberNegative
  override val budgetNumberZero = tableTextSubdued
  override val budgetNumberNeutral = tableText
  override val budgetNumberPositive = budgetNumberNeutral
  override val templateNumberFunded = numberPositive
  override val templateNumberUnderFunded = ColorPalette.orange200
  override val toBudgetPositive = numberPositive
  override val toBudgetZero = numberPositive
  override val toBudgetNegative = budgetNumberNegative

  override val sidebarBackground = ColorPalette.gray900
  override val sidebarItemBackgroundPending = ColorPalette.orange200
  override val sidebarItemBackgroundPositive = ColorPalette.green400
  override val sidebarItemBackgroundFailed = ColorPalette.red300
  override val sidebarItemAccentSelected = ColorPalette.purple200
  override val sidebarItemBackgroundHover = ColorPalette.gray700
  override val sidebarItemText = ColorPalette.gray100
  override val sidebarItemTextUpdated = sidebarItemText
  override val sidebarItemTextSelected = ColorPalette.purple200
  override val sidebarBudgetName = ColorPalette.gray300

  override val menuBackground = ColorPalette.gray700
  override val menuItemBackground = ColorPalette.gray200
  override val menuItemBackgroundHover = ColorPalette.gray500
  override val menuItemText = ColorPalette.gray100
  override val menuItemTextHover = ColorPalette.gray50
  override val menuItemTextSelected = ColorPalette.purple400
  override val menuItemTextHeader = ColorPalette.purple200
  override val menuBorder = ColorPalette.gray800
  override val menuBorderHover = ColorPalette.purple300
  override val menuKeybindingText = ColorPalette.purple200
  override val menuAutoCompleteBackground = ColorPalette.gray600
  override val menuAutoCompleteBackgroundHover = ColorPalette.gray500
  override val menuAutoCompleteText = ColorPalette.gray100
  override val menuAutoCompleteTextHover = ColorPalette.green900
  override val menuAutoCompleteTextHeader = ColorPalette.purple200
  override val menuAutoCompleteItemTextHover = ColorPalette.gray50
  override val menuAutoCompleteItemText = menuItemText

  override val modalBackground = ColorPalette.gray700
  override val modalBorder = ColorPalette.gray200
  override val mobileHeaderBackground = ColorPalette.gray900
  override val mobileHeaderText = ColorPalette.purple200
  override val mobileHeaderTextSubdued = ColorPalette.gray200
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = ColorPalette.gray900
  override val mobileNavBackground = ColorPalette.gray600
  override val mobileNavItem = ColorPalette.gray150
  override val mobileNavItemSelected = ColorPalette.purple200
  override val mobileAccountShadow = cardShadow
  override val mobileAccountText = ColorPalette.blue800
  override val mobileTransactionSelected = ColorPalette.purple300

  // Mobile view themes (for the top bar)
  override val mobileViewTheme = mobileHeaderBackground
  override val mobileConfigServerViewTheme = ColorPalette.purple500

  override val markdownNormal = ColorPalette.purple700
  override val markdownDark = ColorPalette.purple500
  override val markdownLight = ColorPalette.purple800

  // Button
  override val buttonMenuText = ColorPalette.gray200
  override val buttonMenuTextHover = buttonMenuText
  override val buttonMenuBackground = ColorPalette.gray700
  override val buttonMenuBackgroundHover = Color(color = 0x40C8C8C8)
  override val buttonMenuBorder = ColorPalette.gray500
  override val buttonMenuSelectedText = ColorPalette.green800
  override val buttonMenuSelectedTextHover = ColorPalette.orange800
  override val buttonMenuSelectedBackground = ColorPalette.orange200
  override val buttonMenuSelectedBackgroundHover = ColorPalette.gray300
  override val buttonMenuSelectedBorder = buttonMenuSelectedBackground

  override val buttonPrimaryText = ColorPalette.white
  override val buttonPrimaryTextHover = buttonPrimaryText
  override val buttonPrimaryBackground = ColorPalette.purple300
  override val buttonPrimaryBackgroundHover = buttonPrimaryBackground
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x99000000)
  override val buttonPrimaryDisabledText = ColorPalette.gray400
  override val buttonPrimaryDisabledBackground = ColorPalette.gray700
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = ColorPalette.gray150
  override val buttonNormalTextHover = ColorPalette.gray150
  override val buttonNormalBackground = ColorPalette.gray600
  override val buttonNormalBackgroundHover = ColorPalette.gray400
  override val buttonNormalBorder = ColorPalette.gray300
  override val buttonNormalShadow = Color(color = 0x66000000)
  override val buttonNormalSelectedText = ColorPalette.white
  override val buttonNormalSelectedBackground = ColorPalette.purple500
  override val buttonNormalDisabledText = ColorPalette.gray400
  override val buttonNormalDisabledBackground = ColorPalette.gray700
  override val buttonNormalDisabledBorder = ColorPalette.gray500

  override val calendarText = ColorPalette.gray50
  override val calendarBackground = ColorPalette.gray700
  override val calendarItemText = ColorPalette.gray150
  override val calendarItemBackground = ColorPalette.gray500
  override val calendarSelectedBackground = buttonNormalSelectedBackground
  override val calendarCellBackground = ColorPalette.navy900

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0x4DC8C8C8)
  override val buttonBareBackgroundActive = Color(color = 0x80C8C8C8)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = ColorPalette.green600
  override val noticeBackgroundLight = ColorPalette.green900
  override val noticeBackgroundDark = ColorPalette.green400
  override val noticeText = ColorPalette.green300
  override val noticeTextLight = ColorPalette.green400
  override val noticeTextDark = ColorPalette.green150
  override val noticeTextMenu = ColorPalette.green400
  override val noticeTextMenuHover = ColorPalette.green700
  override val noticeBorder = ColorPalette.green800
  override val warningBackground = ColorPalette.orange800
  override val warningText = ColorPalette.orange200
  override val warningTextLight = ColorPalette.orange500
  override val warningTextDark = ColorPalette.orange100
  override val warningBorder = ColorPalette.orange500
  override val errorBackground = ColorPalette.red800
  override val errorText = ColorPalette.red200
  override val errorTextDark = ColorPalette.red150
  override val errorTextDarker = errorTextDark
  override val errorTextMenu = ColorPalette.red200
  override val errorBorder = ColorPalette.red500
  override val upcomingBackground = ColorPalette.purple800
  override val upcomingText = ColorPalette.purple200
  override val upcomingBorder = tableBorder

  override val formLabelText = ColorPalette.purple150
  override val formLabelBackground = ColorPalette.blue900
  override val formInputBackground = ColorPalette.gray800
  override val formInputBackgroundSelected = ColorPalette.gray700
  override val formInputBackgroundSelection = ColorPalette.purple400
  override val formInputBorder = ColorPalette.gray600
  override val formInputBorderSelected = ColorPalette.purple300
  override val formInputText = ColorPalette.gray150
  override val formInputTextSelected = ColorPalette.black
  override val formInputTextPlaceholder = ColorPalette.gray150
  override val formInputTextPlaceholderSelected = ColorPalette.gray100
  override val formInputTextReadOnlySelection = ColorPalette.gray800
  override val formInputTextSelection = ColorPalette.gray800
  override val formInputShadowSelected = ColorPalette.purple400
  override val formInputTextHighlight = ColorPalette.purple200
  override val checkboxText = tableText
  override val checkboxBackgroundSelected = ColorPalette.purple300
  override val checkboxBorderSelected = ColorPalette.purple300
  override val checkboxShadowSelected = ColorPalette.purple500
  override val checkboxToggleBackground = ColorPalette.gray400
  override val checkboxToggleBackgroundSelected = ColorPalette.purple300
  override val checkboxToggleDisabled = ColorPalette.gray700

  override val pillBackground = ColorPalette.gray500
  override val pillBackgroundLight = ColorPalette.gray900
  override val pillText = ColorPalette.gray200
  override val pillTextHighlighted = ColorPalette.purple200
  override val pillBorder = ColorPalette.gray500
  override val pillBorderDark = pillBorder
  override val pillBackgroundSelected = ColorPalette.purple600
  override val pillTextSelected = ColorPalette.gray150
  override val pillBorderSelected = ColorPalette.purple300
  override val pillTextSubdued = ColorPalette.gray500

  override val reportsRed = ColorPalette.red300
  override val reportsBlue = ColorPalette.blue400
  override val reportsGreen = ColorPalette.green400
  override val reportsGray = ColorPalette.gray400
  override val reportsLabel = pageText
  override val reportsInnerLabel = ColorPalette.navy800
  override val reportsNumberPositive = numberPositive
  override val reportsNumberNegative = numberNegative
  override val reportsNumberNeutral = numberNeutral
  override val reportsChartFill = reportsNumberPositive

  override val noteTagBackground = ColorPalette.purple800
  override val noteTagBackgroundHover = ColorPalette.purple600
  override val noteTagDefault = ColorPalette.purple700
  override val noteTagText = ColorPalette.purple100

  override val budgetCurrentMonth = tableBackground
  override val budgetOtherMonth = ColorPalette.gray700
  override val budgetHeaderCurrentMonth = tableHeaderBackground
  override val budgetHeaderOtherMonth = ColorPalette.gray800

  override val floatingActionBarBackground = ColorPalette.gray900
  override val floatingActionBarBorder = ColorPalette.purple300
  override val floatingActionBarText = ColorPalette.purple200

  override val tooltipText = ColorPalette.gray100
  override val tooltipBackground = ColorPalette.gray800
  override val tooltipBorder = ColorPalette.gray600

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
