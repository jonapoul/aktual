package aktual.theme

import aktual.core.model.ThemeId
import androidx.compose.ui.graphics.Color

data object MidnightColors : DefaultColors {
  override val id = ThemeId("Midnight")
  override val isLight = false

  override val accent = ColorPalette.purple400
  override val accentContrast = ColorPalette.white
  override val accentHover = Color(color = 0xFF822FD8)
  override val accentText = Color(color = 0xFFBB91FF)
  override val accentBorder = Color(color = 0xFFA660FF)
  override val accentSubtle = Color(color = 0xFF342553)
  override val accentSubtleText = Color(color = 0xFFBB91FF)

  override val backgroundPage = ColorPalette.gray600
  override val background = ColorPalette.gray800
  override val backgroundRaised = ColorPalette.gray700
  override val backgroundHover = ColorPalette.gray500
  override val backgroundSelected = accentSubtle
  override val border = ColorPalette.gray600
  override val borderStrong = ColorPalette.gray400
  override val text = ColorPalette.gray100
  override val textSecondary = ColorPalette.gray200
  override val textTertiary = ColorPalette.gray300

  override val pageBackground = backgroundPage
  override val pageBackgroundTopLeft = ColorPalette.gray800
  override val pageBackgroundBottomRight = ColorPalette.gray700
  override val pageBackgroundLineTop = ColorPalette.purple300
  override val pageBackgroundLineMid = ColorPalette.gray900
  override val pageBackgroundLineBottom = ColorPalette.gray150
  override val pageText = text
  override val pageTextLight = textSecondary
  override val pageTextSubdued = textTertiary
  override val pageTextDark = text
  override val pageTextPositive = accentText
  override val pageTextLink = accentText
  override val pageTextLinkLight = accentText

  override val cardBackground = background
  override val cardBorder = accentBorder
  override val cardShadow = ColorPalette.gray900

  override val tableBackground = background
  override val tableRowBackgroundAlternate = tableBackground
  override val tableRowBackgroundHover = backgroundHover
  override val tableText = text
  override val tableTextItemAdded = tableText
  override val tableTextLight = tableText
  override val tableTextSubdued = textSecondary
  override val tableTextSelected = tableText
  override val tableTextInactive = ColorPalette.gray300
  override val tableHeaderText = ColorPalette.gray200
  override val tableHeaderBackground = ColorPalette.gray900
  override val tableBorder = border
  override val tableBorderSelected = accentBorder
  override val tableBorderHover = accentBorder
  override val tableBorderSeparator = borderStrong
  override val tableRowBackgroundHighlight = backgroundSelected
  override val tableRowBackgroundHighlightText = tableText
  override val tableRowHeaderBackground = ColorPalette.gray700
  override val tableRowHeaderText = text

  override val numberPositive = ColorPalette.green300
  override val numberNegative = ColorPalette.red200
  override val numberNeutral = textSecondary
  override val budgetNumberNegative = numberNegative
  override val budgetNumberZero = tableTextSubdued
  override val budgetNumberNeutral = tableText
  override val budgetNumberPositive = budgetNumberNeutral
  override val templateNumberFunded = numberPositive
  override val templateNumberUnderFunded = ColorPalette.orange200
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

  override val sidebarBackground = ColorPalette.gray900
  override val sidebarItemBackgroundPending = ColorPalette.orange200
  override val sidebarItemBackgroundPositive = ColorPalette.green400
  override val sidebarItemBackgroundFailed = ColorPalette.red300
  override val sidebarItemAccentSelected = Color(color = 0xFFB380FF)
  override val sidebarItemBackgroundHover = ColorPalette.gray700
  override val sidebarItemBackgroundSelected = sidebarItemAccentSelected.withOpacity(0.22)
  override val sidebarItemText = ColorPalette.gray100
  override val sidebarItemTextUpdated = sidebarItemText
  override val sidebarItemTextSelected = sidebarItemAccentSelected
  override val sidebarBudgetName = ColorPalette.gray300
  override val sidebarHeaderText = ColorPalette.gray100
  override val sidebarTextSubdued = ColorPalette.gray300
  override val sidebarTextMuted = ColorPalette.gray500
  override val sidebarTextPositive = sidebarItemBackgroundPositive
  override val sidebarTextFailed = sidebarItemBackgroundFailed
  override val sidebarBackgroundFailedSubtle = sidebarItemBackgroundFailed.copy(alpha = 0.16f)
  override val sidebarBorder = sidebarItemBackgroundHover
  override val sidebarControlBackground = sidebarItemBackgroundHover
  override val sidebarBrand = sidebarItemAccentSelected

  override val sidebarRedesignBackground = sidebarBackground
  override val sidebarRedesignItemText = sidebarItemText
  override val sidebarRedesignItemBackgroundHover = sidebarItemBackgroundHover
  override val sidebarRedesignItemAccentSelected = sidebarItemAccentSelected
  override val sidebarRedesignItemTextSelected = sidebarItemTextSelected
  override val sidebarRedesignItemBackgroundSelected = sidebarItemBackgroundSelected
  override val sidebarRedesignItemBackgroundFailed = sidebarItemBackgroundFailed
  override val sidebarRedesignHeaderText = sidebarHeaderText
  override val sidebarRedesignTextSubdued = sidebarTextSubdued
  override val sidebarRedesignTextMuted = sidebarTextMuted
  override val sidebarRedesignRightBorder = sidebarBorder

  override val menuBackground = backgroundRaised
  override val menuItemBackgroundHover = ColorPalette.gray500
  override val menuItemText = text
  override val menuItemTextHover = text
  override val menuItemTextSelected = accentText
  override val menuItemTextHeader = Color(color = 0xFFAC6EFF)
  override val menuBorder = border
  override val menuKeybindingText = Color(color = 0xFFAC6EFF)
  override val menuAutoCompleteBackground = ColorPalette.gray600
  override val menuAutoCompleteBackgroundHover = ColorPalette.gray500
  override val menuAutoCompleteText = ColorPalette.gray100
  override val menuAutoCompleteTextHover = ColorPalette.green150
  override val menuAutoCompleteTextHeader = Color(color = 0xFFD9C5FF)
  override val menuAutoCompleteItemTextHover = ColorPalette.gray50
  override val menuAutoCompleteItemText = menuItemText

  override val modalBackground = backgroundRaised
  override val modalBorder = ColorPalette.gray200
  override val mobileHeaderBackground = accent
  override val mobileHeaderText = accentContrast
  override val mobileHeaderTextSubdued = mobileHeaderText.withOpacity(0.75)
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = ColorPalette.gray900
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
  override val buttonPrimaryBackground = accent
  override val buttonPrimaryBackgroundHover = accentHover
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x99000000)
  override val buttonPrimaryDisabledText = ColorPalette.gray400
  override val buttonPrimaryDisabledBackground = ColorPalette.gray700
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = text
  override val buttonNormalTextHover = text
  override val buttonNormalBackground = backgroundRaised
  override val buttonNormalBackgroundHover = backgroundHover
  override val buttonNormalBorder = borderStrong
  override val buttonNormalShadow = Color(color = 0x66000000)
  override val buttonNormalSelectedText = ColorPalette.white
  override val buttonNormalSelectedBackground = accent
  override val buttonNormalDisabledText = ColorPalette.gray400
  override val buttonNormalDisabledBackground = backgroundRaised
  override val buttonNormalDisabledBorder = ColorPalette.gray500

  override val calendarText = ColorPalette.gray50
  override val calendarBackground = ColorPalette.gray700
  override val calendarItemText = ColorPalette.white
  override val calendarItemBackground = ColorPalette.gray500
  override val calendarSelectedBackground = accent
  override val calendarSelectedText = ColorPalette.white
  override val calendarCellBackground = ColorPalette.navy900
  override val datePickerRangeBackground = accentSubtle

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0x4DC8C8C8)
  override val buttonBareBackgroundActive = Color(color = 0x80C8C8C8)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = ColorPalette.green600
  override val noticeBackgroundLight = ColorPalette.green900
  override val noticeText = ColorPalette.green300
  override val noticeTextLight = ColorPalette.green400
  override val noticeTextDark = ColorPalette.green150
  override val noticeTextMenu = ColorPalette.green400
  override val noticeBorder = ColorPalette.green800
  override val warningBackground = ColorPalette.orange800
  override val warningText = ColorPalette.orange200
  override val warningTextDark = ColorPalette.orange100
  override val warningBorder = ColorPalette.orange500
  override val errorBackground = ColorPalette.red800
  override val errorText = ColorPalette.red200
  override val errorTextDark = ColorPalette.red150
  override val errorTextDarker = errorTextDark
  override val errorTextMenu = ColorPalette.red200
  override val errorBorder = ColorPalette.red500
  override val upcomingBackground = accentSubtle
  override val upcomingText = accentSubtleText

  override val formLabelText = accentText
  override val formLabelBackground = tableBackground
  override val formInputBackground = background
  override val formInputBackgroundSelected = ColorPalette.gray700
  override val formInputBackgroundSelection = ColorPalette.purple400
  override val formInputBorder = border
  override val formInputBorderSelected = accentBorder
  override val formInputText = text
  override val formInputTextPlaceholder = textTertiary
  override val formInputTextPlaceholderSelected = ColorPalette.gray100
  override val formInputTextReadOnlySelection = ColorPalette.gray800
  override val formInputShadowSelected = accentBorder
  override val formInputTextHighlight = accentText
  override val checkboxText = accentContrast
  override val checkboxBackgroundSelected = accent
  override val checkboxBorderSelected = accent
  override val checkboxShadowSelected = accentBorder
  override val checkboxToggleBackground = ColorPalette.gray400
  override val checkboxToggleBackgroundSelected = accent
  override val checkboxToggleDisabled = ColorPalette.gray700

  override val pillBackground = ColorPalette.gray500
  override val pillBackgroundLight = ColorPalette.gray900
  override val pillText = text
  override val pillTextHighlighted = Color(color = 0xFFB380FF)
  override val pillBorder = ColorPalette.gray500
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
  override val noteTagBackgroundHover = Color(color = 0xFF241D3A)
  override val noteTagText = accentSubtleText

  override val budgetCurrentMonth = tableBackground
  override val budgetOtherMonth = ColorPalette.gray700
  override val budgetHeaderCurrentMonth = tableHeaderBackground
  override val budgetHeaderOtherMonth = ColorPalette.gray800

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
