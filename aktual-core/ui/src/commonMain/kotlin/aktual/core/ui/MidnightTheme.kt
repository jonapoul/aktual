package aktual.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal class MidnightTheme : Theme {
  override val pageBackground = AktualColors.gray600
  override val pageBackgroundModalActive = AktualColors.gray700
  override val pageBackgroundTopLeft = AktualColors.gray800
  override val pageBackgroundBottomRight = AktualColors.gray700
  override val pageBackgroundLineTop = AktualColors.purple300
  override val pageBackgroundLineMid = AktualColors.gray900
  override val pageBackgroundLineBottom = AktualColors.gray150
  override val pageText = AktualColors.gray100
  override val pageTextLight = AktualColors.gray200
  override val pageTextSubdued = AktualColors.gray400
  override val pageTextDark = AktualColors.gray100
  override val pageTextPositive = AktualColors.purple200
  override val pageTextLink = AktualColors.purple300
  override val pageTextLinkLight = AktualColors.purple300
  override val numberPositive = AktualColors.green300
  override val numberNegative = AktualColors.red200
  override val numberNeutral = AktualColors.gray500

  override val cardBackground = AktualColors.gray800
  override val cardBorder = AktualColors.purple300
  override val cardShadow = AktualColors.gray900

  override val tableBackground = AktualColors.gray800
  override val tableRowBackgroundHover = AktualColors.gray500
  override val tableText = AktualColors.gray150
  override val tableTextLight = tableText
  override val tableTextSubdued = AktualColors.gray500
  override val tableTextSelected = AktualColors.gray800
  override val tableTextHover = AktualColors.gray400
  override val tableTextInactive = AktualColors.gray400
  override val tableHeaderText = AktualColors.gray200
  override val tableHeaderBackground = AktualColors.gray900
  override val tableBorder = AktualColors.gray600
  override val tableBorderSelected = AktualColors.purple400
  override val tableBorderHover = AktualColors.purple300
  override val tableBorderSeparator = AktualColors.gray400
  override val tableRowBackgroundHighlight = AktualColors.purple150
  override val tableRowBackgroundHighlightText = AktualColors.gray800
  override val tableRowHeaderBackground = AktualColors.gray700
  override val tableRowHeaderText = AktualColors.gray150

  override val sidebarBackground = AktualColors.gray900
  override val sidebarItemBackgroundPositive = AktualColors.green400
  override val sidebarItemBackgroundFailed = AktualColors.red300
  override val sidebarItemAccentSelected = AktualColors.purple200
  override val sidebarItemBackgroundHover = AktualColors.gray700
  override val sidebarItemText = AktualColors.gray100
  override val sidebarItemTextSelected = AktualColors.purple200

  override val menuBackground = AktualColors.gray700
  override val menuItemBackground = AktualColors.gray200
  override val menuItemBackgroundHover = AktualColors.gray500
  override val menuItemText = AktualColors.gray100
  override val menuItemTextHover = AktualColors.gray50
  override val menuItemTextSelected = AktualColors.purple400
  override val menuItemTextHeader = AktualColors.purple200
  override val menuBorder = AktualColors.gray800
  override val menuBorderHover = AktualColors.purple300
  override val menuKeybindingText = AktualColors.gray500
  override val menuAutoCompleteBackground = AktualColors.gray600
  override val menuAutoCompleteBackgroundHover = AktualColors.gray400
  override val menuAutoCompleteText = AktualColors.gray100
  override val menuAutoCompleteTextHeader = AktualColors.purple200

  override val modalBackground = AktualColors.gray700
  override val modalBorder = AktualColors.gray200
  override val mobileHeaderBackground = AktualColors.gray900
  override val mobileHeaderBackgroundSubdued = AktualColors.gray800
  override val mobileHeaderText = AktualColors.purple200
  override val mobileHeaderTextTransparent = AktualColors.purple200
  override val mobileHeaderTextSubdued = AktualColors.gray200
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = AktualColors.gray900
  override val mobileNavBackground = AktualColors.gray600
  override val mobileNavItem = AktualColors.gray150
  override val mobileNavItemSelected = AktualColors.purple200
  override val mobileAccountShadow = cardShadow
  override val mobileAccountText = AktualColors.blue800
  override val mobileModalBackground = AktualColors.gray100
  override val mobileModalText = AktualColors.white

  // Mobile view themes (for the top bar)
  override val mobileViewTheme = mobileHeaderBackground
  override val mobileConfigServerViewTheme = AktualColors.purple500

  override val markdownNormal = AktualColors.purple700
  override val markdownDark = AktualColors.purple500
  override val markdownLight = AktualColors.purple800

  // Button
  override val buttonMenuText = AktualColors.gray200
  override val buttonMenuTextHover = buttonMenuText
  override val buttonMenuBackground = AktualColors.gray700
  override val buttonMenuBackgroundHover = Color(color = 0x40C8C8C8)
  override val buttonMenuBorder = AktualColors.gray500
  override val buttonMenuSelectedText = AktualColors.green800
  override val buttonMenuSelectedTextHover = AktualColors.orange800
  override val buttonMenuSelectedBackground = AktualColors.orange200
  override val buttonMenuSelectedBackgroundHover = AktualColors.gray300
  override val buttonMenuSelectedBorder = buttonMenuSelectedBackground

  override val buttonPrimaryText = AktualColors.white
  override val buttonPrimaryTextHover = buttonPrimaryText
  override val buttonPrimaryBackground = AktualColors.purple300
  override val buttonPrimaryBackgroundHover = buttonPrimaryBackground
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x99000000)
  override val buttonPrimaryDisabledText = AktualColors.gray500
  override val buttonPrimaryDisabledBackground = AktualColors.gray700
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = AktualColors.gray150
  override val buttonNormalTextHover = AktualColors.gray150
  override val buttonNormalBackground = AktualColors.gray600
  override val buttonNormalBackgroundHover = AktualColors.gray400
  override val buttonNormalBorder = AktualColors.gray300
  override val buttonNormalShadow = Color(color = 0x66000000)
  override val buttonNormalSelectedText = AktualColors.white
  override val buttonNormalSelectedBackground = AktualColors.purple500
  override val buttonNormalDisabledText = AktualColors.gray500
  override val buttonNormalDisabledBackground = AktualColors.gray700
  override val buttonNormalDisabledBorder = AktualColors.gray500

  override val calendarText = AktualColors.gray50
  override val calendarBackground = AktualColors.gray700
  override val calendarItemText = AktualColors.gray150
  override val calendarItemBackground = AktualColors.gray500
  override val calendarSelectedBackground = buttonNormalSelectedBackground
  override val calendarCellBackground = AktualColors.navy900

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0xFF3C3C3C)
  override val buttonBareBackgroundActive = Color(color = 0xFF646464)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = AktualColors.green600
  override val noticeBackgroundLight = AktualColors.green900
  override val noticeBackgroundDark = AktualColors.green400
  override val noticeText = AktualColors.green300
  override val noticeTextLight = AktualColors.green400
  override val noticeTextDark = AktualColors.green150
  override val noticeTextMenu = AktualColors.green400
  override val noticeBorder = AktualColors.green800
  override val warningBackground = AktualColors.orange800
  override val warningText = AktualColors.orange200
  override val warningTextLight = AktualColors.orange500
  override val warningTextDark = AktualColors.orange100
  override val warningBorder = AktualColors.orange500
  override val errorBackground = AktualColors.red800
  override val errorText = AktualColors.red200
  override val errorTextDark = AktualColors.red150
  override val errorTextDarker = errorTextDark
  override val errorTextMenu = AktualColors.red500
  override val errorBorder = AktualColors.red500
  override val upcomingBackground = AktualColors.purple800
  override val upcomingText = AktualColors.purple200
  override val upcomingBorder = tableBorder
  override val successText = AktualColors.green400

  override val formLabelText = AktualColors.purple150
  override val formLabelBackground = AktualColors.blue900
  override val formInputBackground = AktualColors.gray800
  override val formInputBackgroundSelected = AktualColors.gray700
  override val formInputBackgroundSelection = AktualColors.purple400
  override val formInputBorder = AktualColors.gray600
  override val formInputTextReadOnlySelection = AktualColors.gray800
  override val formInputBorderSelected = AktualColors.purple300
  override val formInputText = AktualColors.gray150
  override val formInputTextSelected = AktualColors.black
  override val formInputTextPlaceholder = AktualColors.gray400
  override val formInputTextPlaceholderSelected = AktualColors.gray100
  override val formInputTextSelection = AktualColors.gray800
  override val formInputShadowSelected = AktualColors.purple400
  override val formInputTextHighlight = AktualColors.purple200
  override val checkboxText = tableText
  override val checkboxBackgroundSelected = AktualColors.purple300
  override val checkboxBorderSelected = AktualColors.purple300
  override val checkboxShadowSelected = AktualColors.purple500
  override val checkboxToggleBackground = AktualColors.gray400
  override val checkboxToggleBackgroundSelected = AktualColors.purple300
  override val checkboxToggleDisabled = AktualColors.gray700

  override val pillBackground = AktualColors.gray500
  override val pillBackgroundLight = AktualColors.gray900
  override val pillText = AktualColors.gray200
  override val pillTextHighlighted = AktualColors.purple200
  override val pillBorder = AktualColors.gray500
  override val pillBorderDark = pillBorder
  override val pillBackgroundSelected = AktualColors.purple600
  override val pillTextSelected = AktualColors.gray150
  override val pillBorderSelected = AktualColors.purple300

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

  override val scrollbar = AktualColors.blue400
  override val scrollbarSelected = AktualColors.blue100

  override val sliderThumb = AktualColors.blue300
  override val sliderActiveTrack = AktualColors.blue600
  override val sliderActiveTick = AktualColors.blue600
  override val sliderInactiveTrack = AktualColors.gray700
  override val sliderInactiveTick = AktualColors.gray800

  override val dialogBackground = AktualColors.gray700
  override val dialogProgressWheelTrack = AktualColors.gray700

  override val budgetItemBackground = buttonNormalBackground
  override val budgetItemTextPrimary = buttonNormalText
  override val budgetItemTextSecondary = pageTextLight
}
