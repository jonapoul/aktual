package dev.jonpoulton.actual.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal class LightColorScheme : ActualColorScheme {
  override val pageBackground = ActualColors.navy100
  override val pageBackgroundModalActive = ActualColors.navy200
  override val pageBackgroundTopLeft = ActualColors.navy100
  override val pageBackgroundBottomRight = ActualColors.blue150
  override val pageBackgroundLineTop = ActualColors.white
  override val pageBackgroundLineMid = ActualColors.navy100
  override val pageBackgroundLineBottom = ActualColors.blue150
  override val pageText = Color(color = 0xFF272630)
  override val pageTextLight = ActualColors.navy500
  override val pageTextSubdued = ActualColors.navy300
  override val pageTextDark = ActualColors.navy800
  override val pageTextPositive = ActualColors.purple600
  override val pageTextLink = ActualColors.blue600
  override val pageTextLinkLight = ActualColors.blue300

  override val cardBackground = ActualColors.white
  override val cardBorder = ActualColors.purple700
  override val cardShadow = ActualColors.navy700

  override val tableBackground = ActualColors.white
  override val tableRowBackgroundHover = ActualColors.navy50
  override val tableText = pageText
  override val tableTextLight = ActualColors.navy400
  override val tableTextSubdued = ActualColors.navy100
  override val tableTextSelected = ActualColors.navy700
  override val tableTextHover = ActualColors.navy900
  override val tableTextInactive = ActualColors.navy500
  override val tableHeaderText = ActualColors.navy600
  override val tableHeaderBackground = ActualColors.white
  override val tableBorder = ActualColors.navy100
  override val tableBorderSelected = ActualColors.purple500
  override val tableBorderHover = ActualColors.purple400
  override val tableBorderSeparator = ActualColors.navy400
  override val tableRowBackgroundHighlight = ActualColors.blue150
  override val tableRowBackgroundHighlightText = ActualColors.navy700
  override val tableRowHeaderBackground = ActualColors.navy50
  override val tableRowHeaderText = ActualColors.navy800

  override val sidebarBackground = ActualColors.navy900
  override val sidebarItemBackgroundPositive = ActualColors.green500
  override val sidebarItemBackgroundFailed = ActualColors.red300
  override val sidebarItemBackgroundHover = ActualColors.navy800
  override val sidebarItemAccentSelected = ActualColors.purple200
  override val sidebarItemText = ActualColors.navy150
  override val sidebarItemTextSelected = ActualColors.purple200

  override val menuBackground = ActualColors.white
  override val menuItemBackground = ActualColors.navy50
  override val menuItemBackgroundHover = ActualColors.navy100
  override val menuItemText = ActualColors.navy900
  override val menuItemTextHover = menuItemText
  override val menuItemTextSelected = ActualColors.purple300
  override val menuItemTextHeader = ActualColors.navy400
  override val menuBorder = ActualColors.navy100
  override val menuBorderHover = ActualColors.purple100
  override val menuKeybindingText = ActualColors.navy400
  override val menuAutoCompleteBackground = ActualColors.navy900
  override val menuAutoCompleteBackgroundHover = ActualColors.navy600
  override val menuAutoCompleteText = ActualColors.white
  override val menuAutoCompleteTextHeader = ActualColors.orange150

  override val modalBackground = ActualColors.white
  override val modalBorder = ActualColors.white
  override val mobileHeaderBackground = ActualColors.purple400
  override val mobileHeaderText = ActualColors.navy50
  override val mobileHeaderTextSubdued = ActualColors.gray200
  override val mobileHeaderTextHover = Color(color = 0x26C8C8C8)
  override val mobilePageBackground = ActualColors.navy50
  override val mobileNavBackground = ActualColors.white
  override val mobileNavItem = ActualColors.gray300
  override val mobileNavItemSelected = ActualColors.purple500
  override val mobileAccountShadow = ActualColors.navy300
  override val mobileAccountText = ActualColors.blue800
  override val mobileModalBackground = ActualColors.navy900
  override val mobileModalText = ActualColors.white

  // Mobile view themes (for the top bar)
  override val mobileViewTheme = mobileHeaderBackground
  override val mobileConfigServerViewTheme = ActualColors.purple500

  override val markdownNormal = ActualColors.purple150
  override val markdownDark = ActualColors.purple400
  override val markdownLight = ActualColors.purple100

  // Button
  override val buttonMenuText = ActualColors.navy100
  override val buttonMenuTextHover = ActualColors.navy50
  override val buttonMenuBackground = Color.Transparent
  override val buttonMenuBackgroundHover = Color(color = 0x40C8C8C8)
  override val buttonMenuBorder = ActualColors.navy500
  override val buttonMenuSelectedText = ActualColors.green800
  override val buttonMenuSelectedTextHover = ActualColors.orange800
  override val buttonMenuSelectedBackground = ActualColors.orange200
  override val buttonMenuSelectedBackgroundHover = ActualColors.orange300
  override val buttonMenuSelectedBorder = buttonMenuSelectedBackground

  override val buttonPrimaryText = ActualColors.white
  override val buttonPrimaryTextHover = buttonPrimaryText
  override val buttonPrimaryBackground = ActualColors.purple500
  override val buttonPrimaryBackgroundHover = ActualColors.purple300
  override val buttonPrimaryBorder = buttonPrimaryBackground
  override val buttonPrimaryShadow = Color(color = 0x4D000000)
  override val buttonPrimaryDisabledText = ActualColors.white
  override val buttonPrimaryDisabledBackground = ActualColors.navy300
  override val buttonPrimaryDisabledBorder = buttonPrimaryDisabledBackground

  override val buttonNormalText = ActualColors.navy900
  override val buttonNormalTextHover = buttonNormalText
  override val buttonNormalBackground = ActualColors.white
  override val buttonNormalBackgroundHover = buttonNormalBackground
  override val buttonNormalBorder = ActualColors.navy150
  override val buttonNormalShadow = Color(color = 0x33000000)
  override val buttonNormalSelectedText = ActualColors.white
  override val buttonNormalSelectedBackground = ActualColors.blue600
  override val buttonNormalDisabledText = ActualColors.navy300
  override val buttonNormalDisabledBackground = buttonNormalBackground
  override val buttonNormalDisabledBorder = buttonNormalBorder

  override val calendarText = ActualColors.navy50
  override val calendarBackground = ActualColors.navy900
  override val calendarItemText = ActualColors.navy150
  override val calendarItemBackground = ActualColors.navy800
  override val calendarSelectedBackground = ActualColors.navy500

  override val buttonBareText = buttonNormalText
  override val buttonBareTextHover = buttonNormalText
  override val buttonBareBackground = Color.Transparent
  override val buttonBareBackgroundHover = Color(color = 0x26646464)
  override val buttonBareBackgroundActive = Color(color = 0x40646464)
  override val buttonBareDisabledText = buttonNormalDisabledText
  override val buttonBareDisabledBackground = buttonBareBackground

  override val noticeBackground = ActualColors.green150
  override val noticeBackgroundLight = ActualColors.green100
  override val noticeBackgroundDark = ActualColors.green500
  override val noticeText = ActualColors.green700
  override val noticeTextLight = ActualColors.green500
  override val noticeTextDark = ActualColors.green900
  override val noticeTextMenu = ActualColors.green200
  override val noticeBorder = ActualColors.green500
  override val warningBackground = ActualColors.orange200
  override val warningText = ActualColors.orange700
  override val warningTextLight = ActualColors.orange500
  override val warningTextDark = ActualColors.orange900
  override val warningBorder = ActualColors.orange500
  override val errorBackground = ActualColors.red100
  override val errorText = ActualColors.red500
  override val errorTextDark = ActualColors.red700
  override val errorTextDarker = ActualColors.red900
  override val errorTextMenu = ActualColors.red200
  override val errorBorder = ActualColors.red500
  override val upcomingBackground = ActualColors.purple100
  override val upcomingText = ActualColors.purple700
  override val upcomingBorder = ActualColors.purple500

  override val formLabelText = ActualColors.blue600
  override val formLabelBackground = ActualColors.blue200
  override val formInputBackground = ActualColors.navy50
  override val formInputBackgroundSelected = ActualColors.white
  override val formInputBackgroundSelection = ActualColors.purple500
  override val formInputBorder = ActualColors.navy150
  override val formInputTextReadOnlySelection = ActualColors.navy50
  override val formInputBorderSelected = ActualColors.purple500
  override val formInputText = ActualColors.navy900
  override val formInputTextSelected = ActualColors.navy50
  override val formInputTextPlaceholder = ActualColors.navy300
  override val formInputTextPlaceholderSelected = ActualColors.navy200
  override val formInputTextSelection = ActualColors.navy100
  override val formInputShadowSelected = ActualColors.purple300
  override val formInputTextHighlight = ActualColors.purple200
  override val checkboxText = tableBackground
  override val checkboxBackgroundSelected = ActualColors.blue500
  override val checkboxBorderSelected = ActualColors.blue500
  override val checkboxShadowSelected = ActualColors.blue300
  override val checkboxToggleBackground = ActualColors.gray400

  override val pillBackground = ActualColors.navy150
  override val pillBackgroundLight = ActualColors.navy100
  override val pillText = ActualColors.navy800
  override val pillTextHighlighted = ActualColors.purple600
  override val pillBorder = ActualColors.navy150
  override val pillBorderDark = ActualColors.navy300
  override val pillBackgroundSelected = ActualColors.blue150
  override val pillTextSelected = ActualColors.blue900
  override val pillBorderSelected = ActualColors.purple500

  override val reportsRed = ActualColors.red300
  override val reportsBlue = ActualColors.blue400
  override val reportsLabel = ActualColors.navy900
  override val reportsInnerLabel = ActualColors.navy800
}
