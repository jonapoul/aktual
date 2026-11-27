package aktual.theme

import aktual.core.model.ThemeId
import androidx.compose.ui.graphics.Color

data object LightColors : DefaultColors {
  override val id = ThemeId("Light")
  override val isLight = true

  override val accent = ColorPalette.purple500
  override val accentContrast = ColorPalette.white
  override val accentHover = Color(color = 0xFF7300C3)
  override val accentText = ColorPalette.purple500
  override val accentBorder = ColorPalette.purple500
  override val accentSubtle = Color(color = 0xFFEFE7FE)
  override val accentSubtleText = ColorPalette.purple500

  override val backgroundPage = ColorPalette.navy100
  override val background = ColorPalette.white
  override val backgroundRaised = ColorPalette.white
  override val backgroundHover = ColorPalette.navy50
  override val backgroundSelected = accentSubtle
  override val border = ColorPalette.navy100
  override val borderStrong = ColorPalette.navy300
  override val text = Color(color = 0xFF272630)
  override val textSecondary = ColorPalette.navy500
  override val textTertiary = ColorPalette.navy300

  override val pageBackground = backgroundPage
  override val pageBackgroundTopLeft = ColorPalette.navy100
  override val pageBackgroundBottomRight = ColorPalette.blue150
  override val pageBackgroundLineTop = ColorPalette.white
  override val pageBackgroundLineMid = ColorPalette.navy100
  override val pageBackgroundLineBottom = ColorPalette.blue150
  override val pageText = text
  override val pageTextLight = textSecondary
  override val pageTextSubdued = textTertiary
  override val pageTextDark = text
  override val pageTextPositive = accentText
  override val pageTextLink = accentText
  override val pageTextLinkLight = accentText

  override val cardBackground = background
  override val cardBorder = accentBorder
  override val cardShadow = ColorPalette.navy700

  override val tableBackground = background
  override val tableRowBackgroundAlternate = tableBackground
  override val tableRowBackgroundHover = backgroundHover
  override val tableText = text
  override val tableTextItemAdded = tableText
  override val tableTextLight = ColorPalette.navy400
  override val tableTextSubdued = textSecondary
  override val tableTextSelected = tableText
  override val tableTextInactive = ColorPalette.navy500
  override val tableHeaderText = ColorPalette.navy600
  override val tableHeaderBackground = ColorPalette.white
  override val tableBorder = border
  override val tableBorderSelected = accentBorder
  override val tableBorderHover = accentBorder
  override val tableBorderSeparator = borderStrong
  override val tableRowBackgroundHighlight = backgroundSelected
  override val tableRowBackgroundHighlightText = tableText
  override val tableRowHeaderBackground = ColorPalette.navy50
  override val tableRowHeaderText = text

  override val numberPositive = ColorPalette.green700
  override val numberNegative = ColorPalette.red500
  override val numberNeutral = textSecondary
  override val budgetNumberNegative = numberNegative
  override val budgetNumberZero = tableTextSubdued
  override val budgetNumberNeutral = tableText
  override val budgetNumberPositive = budgetNumberNeutral
  override val templateNumberFunded = numberPositive
  override val templateNumberUnderFunded = ColorPalette.orange700
  override val toBudgetPositive = numberPositive
  override val toBudgetZero = numberPositive
  override val toBudgetNegative = budgetNumberNegative

  override val settingsCardBackground = tableBackground
  override val settingsCardBorder = tableBorder
  override val settingsCardText = tableText
  override val settingsCardTextSubdued = textSecondary
  override val settingsNavItemText = pageText
  override val settingsNavItemAccentSelected = accentSubtleText
  override val settingsNavItemTextSelected = settingsNavItemAccentSelected
  override val settingsNavItemBackgroundHover = settingsNavItemText.withOpacity(0.08)
  override val settingsNavItemBackgroundSelected = accentSubtle

  override val sidebarBackground = ColorPalette.navy900
  override val sidebarItemBackgroundPending = ColorPalette.orange200
  override val sidebarItemBackgroundPositive = ColorPalette.green500
  override val sidebarItemBackgroundFailed = ColorPalette.red300
  override val sidebarItemBackgroundHover = ColorPalette.navy800
  override val sidebarItemAccentSelected = Color(color = 0xFFC7A2FF)
  override val sidebarItemBackgroundSelected = sidebarItemAccentSelected.withOpacity(0.22)
  override val sidebarItemText = ColorPalette.navy150
  override val sidebarItemTextUpdated = sidebarItemText
  override val sidebarItemTextSelected = sidebarItemAccentSelected
  override val sidebarBudgetName = ColorPalette.navy150
  override val sidebarHeaderText = ColorPalette.white
  override val sidebarTextSubdued = ColorPalette.navy300
  override val sidebarTextMuted = ColorPalette.navy500
  override val sidebarTextPositive = sidebarItemBackgroundPositive
  override val sidebarTextFailed = sidebarItemBackgroundFailed
  override val sidebarBackgroundFailedSubtle = sidebarItemBackgroundFailed.withOpacity(0.16)
  override val sidebarBorder = sidebarItemBackgroundHover
  override val sidebarControlBackground = sidebarItemBackgroundHover
  override val sidebarBrand = sidebarItemAccentSelected

  override val sidebarRedesignBackground = ColorPalette.white
  override val sidebarRedesignItemText = ColorPalette.navy800
  override val sidebarRedesignItemBackgroundHover = ColorPalette.navy100
  override val sidebarRedesignItemAccentSelected = accentBorder
  override val sidebarRedesignItemTextSelected = accentSubtleText
  override val sidebarRedesignItemBackgroundSelected = accentSubtle
  override val sidebarRedesignItemBackgroundFailed = ColorPalette.red500
  override val sidebarRedesignHeaderText = ColorPalette.gray700
  override val sidebarRedesignTextSubdued = ColorPalette.navy600
  override val sidebarRedesignTextMuted = ColorPalette.navy400
  override val sidebarRedesignRightBorder = ColorPalette.border

  override val menuBackground = backgroundRaised
  override val menuItemBackgroundHover = ColorPalette.navy100
  override val menuItemText = text
  override val menuItemTextHover = text
  override val menuItemTextSelected = accentText
  override val menuItemTextHeader = ColorPalette.purple500
  override val menuBorder = border
  override val menuKeybindingText = ColorPalette.purple500
  override val menuAutoCompleteBackground = ColorPalette.navy900
  override val menuAutoCompleteBackgroundHover = ColorPalette.navy600
  override val menuAutoCompleteText = ColorPalette.white
  override val menuAutoCompleteTextHover = ColorPalette.green150
  override val menuAutoCompleteTextHeader = Color(color = 0xFFC7A2FF)
  override val menuAutoCompleteItemTextHover = menuAutoCompleteText
  override val menuAutoCompleteItemText = menuAutoCompleteText

  override val modalBackground = backgroundRaised
  override val modalBorder = ColorPalette.white
  override val mobileHeaderBackground = accent
  override val mobileHeaderText = accentContrast
  override val mobileHeaderTextSubdued = mobileHeaderText.withOpacity(0.75)
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = ColorPalette.navy50
  override val mobileNavBackground = backgroundRaised
  override val mobileNavItem = text
  override val mobileNavItemSelected = accentText
  override val mobileAccountText = ColorPalette.blue800
  override val mobileTransactionSelected = accentBorder

  // Mobile view themes (for the top bar)
  override val mobileViewTheme = mobileHeaderBackground
  override val mobileConfigServerViewTheme = accent

  override val markdownNormal = accent.withOpacity(0.3)
  override val markdownDark = accentBorder
  override val markdownLight = accentSubtle

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
  override val buttonPrimaryBackground = accent
  override val buttonPrimaryBackgroundHover = accentHover
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x4D000000)
  override val buttonPrimaryDisabledText = ColorPalette.white
  override val buttonPrimaryDisabledBackground = ColorPalette.navy300
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = text
  override val buttonNormalTextHover = text
  override val buttonNormalBackground = backgroundRaised
  override val buttonNormalBackgroundHover = backgroundHover
  override val buttonNormalBorder = borderStrong
  override val buttonNormalShadow = Color(color = 0x33000000)
  override val buttonNormalSelectedText = ColorPalette.white
  override val buttonNormalSelectedBackground = accent
  override val buttonNormalDisabledText = ColorPalette.navy300
  override val buttonNormalDisabledBackground = backgroundRaised
  override val buttonNormalDisabledBorder = ColorPalette.navy150

  override val calendarText = ColorPalette.navy50
  override val calendarBackground = ColorPalette.navy900
  override val calendarItemText = ColorPalette.white
  override val calendarItemBackground = ColorPalette.navy800
  override val calendarSelectedBackground = accent
  override val calendarSelectedText = ColorPalette.white
  override val calendarCellBackground = ColorPalette.navy100
  override val datePickerRangeBackground = accentSubtle

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0xFFE7E1E8)
  override val buttonBareBackgroundActive = Color(color = 0xFFD8D2D8)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = ColorPalette.green150
  override val noticeBackgroundLight = ColorPalette.green100
  override val noticeText = ColorPalette.green700
  override val noticeTextLight = ColorPalette.green500
  override val noticeTextDark = ColorPalette.green900
  override val noticeTextMenu = ColorPalette.green200
  override val noticeBorder = ColorPalette.green500
  override val warningBackground = ColorPalette.orange200
  override val warningText = ColorPalette.orange700
  override val warningTextDark = ColorPalette.orange900
  override val warningBorder = ColorPalette.orange500
  override val errorBackground = ColorPalette.red100
  override val errorText = ColorPalette.red500
  override val errorTextDark = ColorPalette.red700
  override val errorTextDarker = ColorPalette.red900
  override val errorTextMenu = ColorPalette.red200
  override val errorBorder = ColorPalette.red500
  override val upcomingBackground = accentSubtle
  override val upcomingText = accentSubtleText

  override val formLabelText = accentText
  override val formLabelBackground = tableBackground
  override val formInputBackground = background
  override val formInputBackgroundSelected = ColorPalette.white
  override val formInputBackgroundSelection = ColorPalette.purple500
  override val formInputBorder = border
  override val formInputBorderSelected = accentBorder
  override val formInputText = text
  override val formInputTextPlaceholder = textTertiary
  override val formInputTextPlaceholderSelected = ColorPalette.navy200
  override val formInputTextReadOnlySelection = ColorPalette.navy50
  override val formInputShadowSelected = accentBorder
  override val formInputTextHighlight = accentText
  override val checkboxText = accentContrast
  override val checkboxBackgroundSelected = accent
  override val checkboxBorderSelected = accent
  override val checkboxShadowSelected = accentBorder
  override val checkboxToggleBackground = ColorPalette.gray400
  override val checkboxToggleBackgroundSelected = accent
  override val checkboxToggleDisabled = ColorPalette.gray200

  override val pillBackground = ColorPalette.navy150
  override val pillBackgroundLight = ColorPalette.navy50
  override val pillText = text
  override val pillTextHighlighted = Color(color = 0xFF7D00D3)
  override val pillBorder = ColorPalette.navy150
  override val pillBorderDark = borderStrong
  override val pillBackgroundSelected = accentSubtle
  override val pillTextSelected = accentSubtleText
  override val pillBorderSelected = accentBorder

  override val reportsRed = ColorPalette.red300
  override val reportsBlue = ColorPalette.blue400
  override val reportsGreen = ColorPalette.green400
  override val reportsGray = ColorPalette.gray400
  override val reportsLabel = text
  override val reportsInnerLabel = ColorPalette.navy800
  override val reportsNumberPositive = numberPositive
  override val reportsNumberNegative = numberNegative
  override val reportsNumberNeutral = numberNeutral
  override val reportsChartFill = reportsNumberPositive

  override val noteTagBackground = accentSubtle
  override val noteTagBackgroundHover = Color(color = 0xFFF7F3FF)
  override val noteTagText = accentSubtleText

  override val budgetCurrentMonth = tableBackground
  override val budgetOtherMonth = ColorPalette.gray50
  override val budgetHeaderCurrentMonth = budgetOtherMonth
  override val budgetHeaderOtherMonth = ColorPalette.gray80

  override val floatingActionBarBackground = accent
  override val floatingActionBarBorder = floatingActionBarBackground
  override val floatingActionBarText = accentContrast

  override val tooltipText = text
  override val tooltipBackground = backgroundRaised
  override val tooltipBorder = border

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
