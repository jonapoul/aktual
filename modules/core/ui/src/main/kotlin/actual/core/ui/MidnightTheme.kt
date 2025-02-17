package actual.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal class MidnightTheme : Theme {
  override val pageBackground = ActualColors.gray600
  override val pageBackgroundModalActive = ActualColors.gray700
  override val pageBackgroundTopLeft = ActualColors.gray800
  override val pageBackgroundBottomRight = ActualColors.gray700
  override val pageBackgroundLineTop = ActualColors.purple300
  override val pageBackgroundLineMid = ActualColors.gray900
  override val pageBackgroundLineBottom = ActualColors.gray150
  override val pageText = ActualColors.gray100
  override val pageTextLight = ActualColors.gray200
  override val pageTextSubdued = ActualColors.gray400
  override val pageTextDark = ActualColors.gray100
  override val pageTextPositive = ActualColors.purple200
  override val pageTextLink = ActualColors.purple300
  override val pageTextLinkLight = ActualColors.purple300

  override val cardBackground = ActualColors.gray800
  override val cardBorder = ActualColors.purple300
  override val cardShadow = ActualColors.gray900

  override val tableBackground = ActualColors.gray800
  override val tableRowBackgroundHover = ActualColors.gray500
  override val tableText = ActualColors.gray150
  override val tableTextLight = tableText
  override val tableTextSubdued = ActualColors.gray500
  override val tableTextSelected = ActualColors.gray800
  override val tableTextHover = ActualColors.gray400
  override val tableTextInactive = ActualColors.gray400
  override val tableHeaderText = ActualColors.gray200
  override val tableHeaderBackground = ActualColors.gray900
  override val tableBorder = ActualColors.gray600
  override val tableBorderSelected = ActualColors.purple400
  override val tableBorderHover = ActualColors.purple300
  override val tableBorderSeparator = ActualColors.gray400
  override val tableRowBackgroundHighlight = ActualColors.purple150
  override val tableRowBackgroundHighlightText = ActualColors.gray800
  override val tableRowHeaderBackground = ActualColors.gray700
  override val tableRowHeaderText = ActualColors.gray150

  override val sidebarBackground = ActualColors.gray900
  override val sidebarItemBackgroundPositive = ActualColors.green400
  override val sidebarItemBackgroundFailed = ActualColors.red300
  override val sidebarItemAccentSelected = ActualColors.purple200
  override val sidebarItemBackgroundHover = ActualColors.gray700
  override val sidebarItemText = ActualColors.gray100
  override val sidebarItemTextSelected = ActualColors.purple200

  override val menuBackground = ActualColors.gray700
  override val menuItemBackground = ActualColors.gray200
  override val menuItemBackgroundHover = ActualColors.gray500
  override val menuItemText = ActualColors.gray100
  override val menuItemTextHover = ActualColors.gray50
  override val menuItemTextSelected = ActualColors.purple400
  override val menuItemTextHeader = ActualColors.purple200
  override val menuBorder = ActualColors.gray800
  override val menuBorderHover = ActualColors.purple300
  override val menuKeybindingText = ActualColors.gray500
  override val menuAutoCompleteBackground = ActualColors.gray600
  override val menuAutoCompleteBackgroundHover = ActualColors.gray400
  override val menuAutoCompleteText = ActualColors.gray100
  override val menuAutoCompleteTextHeader = ActualColors.purple200

  override val modalBackground = ActualColors.gray700
  override val modalBorder = ActualColors.gray200
  override val mobileHeaderBackground = ActualColors.gray900
  override val mobileHeaderBackgroundSubdued = ActualColors.gray800
  override val mobileHeaderText = ActualColors.purple200
  override val mobileHeaderTextSubdued = ActualColors.gray200
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = ActualColors.gray900
  override val mobileNavBackground = ActualColors.gray600
  override val mobileNavItem = ActualColors.gray150
  override val mobileNavItemSelected = ActualColors.purple200
  override val mobileAccountShadow = cardShadow
  override val mobileAccountText = ActualColors.blue800
  override val mobileModalBackground = ActualColors.gray100
  override val mobileModalText = ActualColors.white

  // Mobile view themes (for the top bar)
  override val mobileViewTheme = mobileHeaderBackground
  override val mobileConfigServerViewTheme = ActualColors.purple500

  override val markdownNormal = ActualColors.purple700
  override val markdownDark = ActualColors.purple500
  override val markdownLight = ActualColors.purple800

  // Button
  override val buttonMenuText = ActualColors.gray200
  override val buttonMenuTextHover = buttonMenuText
  override val buttonMenuBackground = ActualColors.gray700
  override val buttonMenuBackgroundHover = Color(color = 0x40C8C8C8)
  override val buttonMenuBorder = ActualColors.gray500
  override val buttonMenuSelectedText = ActualColors.green800
  override val buttonMenuSelectedTextHover = ActualColors.orange800
  override val buttonMenuSelectedBackground = ActualColors.orange200
  override val buttonMenuSelectedBackgroundHover = ActualColors.gray300
  override val buttonMenuSelectedBorder = buttonMenuSelectedBackground

  override val buttonPrimaryText = ActualColors.white
  override val buttonPrimaryTextHover = buttonPrimaryText
  override val buttonPrimaryBackground = ActualColors.purple300
  override val buttonPrimaryBackgroundHover = buttonPrimaryBackground
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x99000000)
  override val buttonPrimaryDisabledText = ActualColors.gray400
  override val buttonPrimaryDisabledBackground = ActualColors.gray700
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = ActualColors.gray150
  override val buttonNormalTextHover = ActualColors.gray150
  override val buttonNormalBackground = ActualColors.gray600
  override val buttonNormalBackgroundHover = ActualColors.gray400
  override val buttonNormalBorder = ActualColors.gray300
  override val buttonNormalShadow = Color(color = 0x66000000)
  override val buttonNormalSelectedText = ActualColors.white
  override val buttonNormalSelectedBackground = ActualColors.purple500
  override val buttonNormalDisabledText = ActualColors.gray500
  override val buttonNormalDisabledBackground = ActualColors.gray700
  override val buttonNormalDisabledBorder = ActualColors.gray500

  override val calendarText = ActualColors.gray50
  override val calendarBackground = ActualColors.gray700
  override val calendarItemText = ActualColors.gray150
  override val calendarItemBackground = ActualColors.gray500
  override val calendarSelectedBackground = buttonNormalSelectedBackground

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0x4DC8C8C8)
  override val buttonBareBackgroundActive = Color(color = 0x80C8C8C8)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = ActualColors.green600
  override val noticeBackgroundLight = ActualColors.green900
  override val noticeBackgroundDark = ActualColors.green400
  override val noticeText = ActualColors.green300
  override val noticeTextLight = ActualColors.green400
  override val noticeTextDark = ActualColors.green150
  override val noticeTextMenu = ActualColors.green400
  override val noticeBorder = ActualColors.green800
  override val warningBackground = ActualColors.orange800
  override val warningText = ActualColors.orange200
  override val warningTextLight = ActualColors.orange500
  override val warningTextDark = ActualColors.orange100
  override val warningBorder = ActualColors.orange500
  override val errorBackground = ActualColors.red800
  override val errorText = ActualColors.red200
  override val errorTextDark = ActualColors.red150
  override val errorTextDarker = errorTextDark
  override val errorTextMenu = ActualColors.red500
  override val errorBorder = ActualColors.red500
  override val upcomingBackground = ActualColors.purple800
  override val upcomingText = ActualColors.purple200
  override val upcomingBorder = tableBorder
  override val successText = ActualColors.green400

  override val formLabelText = ActualColors.purple150
  override val formLabelBackground = ActualColors.blue900
  override val formInputBackground = ActualColors.gray800
  override val formInputBackgroundSelected = ActualColors.gray700
  override val formInputBackgroundSelection = ActualColors.purple400
  override val formInputBorder = ActualColors.gray600
  override val formInputTextReadOnlySelection = ActualColors.gray800
  override val formInputBorderSelected = ActualColors.purple300
  override val formInputText = ActualColors.gray150
  override val formInputTextSelected = ActualColors.black
  override val formInputTextPlaceholder = ActualColors.gray400
  override val formInputTextPlaceholderSelected = ActualColors.gray100
  override val formInputTextSelection = ActualColors.gray800
  override val formInputShadowSelected = ActualColors.purple400
  override val formInputTextHighlight = ActualColors.purple200
  override val checkboxText = tableText
  override val checkboxToggleBackground = ActualColors.gray400
  override val checkboxBackgroundSelected = ActualColors.purple300
  override val checkboxBorderSelected = ActualColors.purple300
  override val checkboxShadowSelected = ActualColors.purple500

  override val pillBackground = ActualColors.gray500
  override val pillBackgroundLight = ActualColors.gray900
  override val pillText = ActualColors.gray200
  override val pillTextHighlighted = ActualColors.purple200
  override val pillBorder = ActualColors.gray500
  override val pillBorderDark = pillBorder
  override val pillBackgroundSelected = ActualColors.purple600
  override val pillTextSelected = ActualColors.gray150
  override val pillBorderSelected = ActualColors.purple300

  override val reportsRed = ActualColors.red300
  override val reportsBlue = ActualColors.blue400
  override val reportsLabel = pageText
  override val reportsInnerLabel = ActualColors.navy800

  override val scrollbar = ActualColors.blue400
  override val scrollbarSelected = ActualColors.blue100

  override val sliderThumb = ActualColors.blue300
  override val sliderActiveTrack = ActualColors.blue600
  override val sliderActiveTick = ActualColors.blue600
  override val sliderInactiveTrack = ActualColors.gray700
  override val sliderInactiveTick = ActualColors.gray800

  override val dialogBackground = ActualColors.gray700
  override val dialogProgressWheelTrack = ActualColors.gray700

  override val budgetItemBackground = buttonNormalBackground
  override val budgetItemTextPrimary = buttonNormalText
  override val budgetItemTextSecondary = pageTextLight
}
