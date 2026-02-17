package aktual.core.theme

import androidx.compose.ui.graphics.Color
import logcat.logcat

internal fun parseTheme(css: String): CustomTheme {
  val attributes: MutableMap<String, Color> =
    css
      .lineSequence()
      .map { it.trim() }
      .filter { it.startsWith("--color-") }
      .associate(::parseAttributePair)
      .toMutableMap()

  fun String.attr(): Color =
    requireNotNull(attributes.remove(this)) {
      "Key '$this' not found in CSS attributes: $attributes"
    }

  val theme =
    CustomTheme(
      page =
        PageColors(
          pageBackground = "pageBackground".attr(),
          pageBackgroundModalActive = "pageBackgroundModalActive".attr(),
          pageBackgroundTopLeft = "pageBackgroundTopLeft".attr(),
          pageBackgroundBottomRight = "pageBackgroundBottomRight".attr(),
          pageBackgroundLineTop = "pageBackgroundLineTop".attr(),
          pageBackgroundLineMid = "pageBackgroundLineMid".attr(),
          pageBackgroundLineBottom = "pageBackgroundLineBottom".attr(),
          pageText = "pageText".attr(),
          pageTextLight = "pageTextLight".attr(),
          pageTextSubdued = "pageTextSubdued".attr(),
          pageTextDark = "pageTextDark".attr(),
          pageTextPositive = "pageTextPositive".attr(),
          pageTextLink = "pageTextLink".attr(),
          pageTextLinkLight = "pageTextLinkLight".attr(),
          numberPositive = "numberPositive".attr(),
          numberNegative = "numberNegative".attr(),
          numberNeutral = "numberNeutral".attr(),
          cardBackground = "cardBackground".attr(),
          cardBorder = "cardBorder".attr(),
          cardShadow = "cardShadow".attr(),
        ),
      table =
        TableColors(
          tableBackground = "tableBackground".attr(),
          tableRowBackgroundHover = "tableRowBackgroundHover".attr(),
          tableText = "tableText".attr(),
          tableTextLight = "tableTextLight".attr(),
          tableTextSubdued = "tableTextSubdued".attr(),
          tableTextSelected = "tableTextSelected".attr(),
          tableTextHover = "tableTextHover".attr(),
          tableTextInactive = "tableTextInactive".attr(),
          tableHeaderText = "tableHeaderText".attr(),
          tableHeaderBackground = "tableHeaderBackground".attr(),
          tableBorder = "tableBorder".attr(),
          tableBorderSelected = "tableBorderSelected".attr(),
          tableBorderHover = "tableBorderHover".attr(),
          tableBorderSeparator = "tableBorderSeparator".attr(),
          tableRowBackgroundHighlight = "tableRowBackgroundHighlight".attr(),
          tableRowBackgroundHighlightText = "tableRowBackgroundHighlightText".attr(),
          tableRowHeaderBackground = "tableRowHeaderBackground".attr(),
          tableRowHeaderText = "tableRowHeaderText".attr(),
        ),
      navigation =
        NavigationColors(
          sidebarBackground = "sidebarBackground".attr(),
          sidebarItemBackgroundPending = "sidebarItemBackgroundPending".attr(),
          sidebarItemBackgroundPositive = "sidebarItemBackgroundPositive".attr(),
          sidebarItemBackgroundFailed = "sidebarItemBackgroundFailed".attr(),
          sidebarItemAccentSelected = "sidebarItemAccentSelected".attr(),
          sidebarItemBackgroundHover = "sidebarItemBackgroundHover".attr(),
          sidebarItemText = "sidebarItemText".attr(),
          sidebarItemTextSelected = "sidebarItemTextSelected".attr(),
          sidebarBudgetName = "sidebarBudgetName".attr(),
          menuBackground = "menuBackground".attr(),
          menuItemBackground = "menuItemBackground".attr(),
          menuItemBackgroundHover = "menuItemBackgroundHover".attr(),
          menuItemText = "menuItemText".attr(),
          menuItemTextHover = "menuItemTextHover".attr(),
          menuItemTextSelected = "menuItemTextSelected".attr(),
          menuItemTextHeader = "menuItemTextHeader".attr(),
          menuBorder = "menuBorder".attr(),
          menuBorderHover = "menuBorderHover".attr(),
          menuKeybindingText = "menuKeybindingText".attr(),
          menuAutoCompleteBackground = "menuAutoCompleteBackground".attr(),
          menuAutoCompleteBackgroundHover = "menuAutoCompleteBackgroundHover".attr(),
          menuAutoCompleteText = "menuAutoCompleteText".attr(),
          menuAutoCompleteTextHover = "menuAutoCompleteTextHover".attr(),
          menuAutoCompleteTextHeader = "menuAutoCompleteTextHeader".attr(),
          menuAutoCompleteItemTextHover = "menuAutoCompleteItemTextHover".attr(),
          menuAutoCompleteItemText = "menuAutoCompleteItemText".attr(),
        ),
      modalMobile =
        ModalMobileColors(
          modalBackground = "modalBackground".attr(),
          modalBorder = "modalBorder".attr(),
          mobileHeaderBackground = "mobileHeaderBackground".attr(),
          mobileHeaderText = "mobileHeaderText".attr(),
          mobileHeaderTextSubdued = "mobileHeaderTextSubdued".attr(),
          mobileHeaderTextHover = "mobileHeaderTextHover".attr(),
          mobilePageBackground = "mobilePageBackground".attr(),
          mobileNavBackground = "mobileNavBackground".attr(),
          mobileNavItem = "mobileNavItem".attr(),
          mobileNavItemSelected = "mobileNavItemSelected".attr(),
          mobileAccountShadow = "mobileAccountShadow".attr(),
          mobileAccountText = "mobileAccountText".attr(),
          mobileTransactionSelected = "mobileTransactionSelected".attr(),
          mobileViewTheme = "mobileViewTheme".attr(),
          mobileConfigServerViewTheme = "mobileConfigServerViewTheme".attr(),
          markdownNormal = "markdownNormal".attr(),
          markdownDark = "markdownDark".attr(),
          markdownLight = "markdownLight".attr(),
        ),
      button =
        ButtonColors(
          buttonMenuText = "buttonMenuText".attr(),
          buttonMenuTextHover = "buttonMenuTextHover".attr(),
          buttonMenuBackground = "buttonMenuBackground".attr(),
          buttonMenuBackgroundHover = "buttonMenuBackgroundHover".attr(),
          buttonMenuBorder = "buttonMenuBorder".attr(),
          buttonMenuSelectedText = "buttonMenuSelectedText".attr(),
          buttonMenuSelectedTextHover = "buttonMenuSelectedTextHover".attr(),
          buttonMenuSelectedBackground = "buttonMenuSelectedBackground".attr(),
          buttonMenuSelectedBackgroundHover = "buttonMenuSelectedBackgroundHover".attr(),
          buttonMenuSelectedBorder = "buttonMenuSelectedBorder".attr(),
          buttonPrimaryText = "buttonPrimaryText".attr(),
          buttonPrimaryTextHover = "buttonPrimaryTextHover".attr(),
          buttonPrimaryBackground = "buttonPrimaryBackground".attr(),
          buttonPrimaryBackgroundHover = "buttonPrimaryBackgroundHover".attr(),
          buttonPrimaryBorder = "buttonPrimaryBorder".attr(),
          buttonPrimaryShadow = "buttonPrimaryShadow".attr(),
          buttonPrimaryDisabledText = "buttonPrimaryDisabledText".attr(),
          buttonPrimaryDisabledBackground = "buttonPrimaryDisabledBackground".attr(),
          buttonPrimaryDisabledBorder = "buttonPrimaryDisabledBorder".attr(),
          buttonNormalText = "buttonNormalText".attr(),
          buttonNormalTextHover = "buttonNormalTextHover".attr(),
          buttonNormalBackground = "buttonNormalBackground".attr(),
          buttonNormalBackgroundHover = "buttonNormalBackgroundHover".attr(),
          buttonNormalBorder = "buttonNormalBorder".attr(),
          buttonNormalShadow = "buttonNormalShadow".attr(),
          buttonNormalSelectedText = "buttonNormalSelectedText".attr(),
          buttonNormalSelectedBackground = "buttonNormalSelectedBackground".attr(),
          buttonNormalDisabledText = "buttonNormalDisabledText".attr(),
          buttonNormalDisabledBackground = "buttonNormalDisabledBackground".attr(),
          buttonNormalDisabledBorder = "buttonNormalDisabledBorder".attr(),
          calendarText = "calendarText".attr(),
          calendarBackground = "calendarBackground".attr(),
          calendarItemText = "calendarItemText".attr(),
          calendarItemBackground = "calendarItemBackground".attr(),
          calendarSelectedBackground = "calendarSelectedBackground".attr(),
          calendarCellBackground = "calendarCellBackground".attr(),
          buttonBareText = "buttonBareText".attr(),
          buttonBareTextHover = "buttonBareTextHover".attr(),
          buttonBareBackground = "buttonBareBackground".attr(),
          buttonBareBackgroundHover = "buttonBareBackgroundHover".attr(),
          buttonBareBackgroundActive = "buttonBareBackgroundActive".attr(),
          buttonBareDisabledText = "buttonBareDisabledText".attr(),
          buttonBareDisabledBackground = "buttonBareDisabledBackground".attr(),
        ),
      status =
        StatusColors(
          noticeBackground = "noticeBackground".attr(),
          noticeBackgroundLight = "noticeBackgroundLight".attr(),
          noticeBackgroundDark = "noticeBackgroundDark".attr(),
          noticeText = "noticeText".attr(),
          noticeTextLight = "noticeTextLight".attr(),
          noticeTextDark = "noticeTextDark".attr(),
          noticeTextMenu = "noticeTextMenu".attr(),
          noticeBorder = "noticeBorder".attr(),
          warningBackground = "warningBackground".attr(),
          warningText = "warningText".attr(),
          warningTextLight = "warningTextLight".attr(),
          warningTextDark = "warningTextDark".attr(),
          warningBorder = "warningBorder".attr(),
          errorBackground = "errorBackground".attr(),
          errorText = "errorText".attr(),
          errorTextDark = "errorTextDark".attr(),
          errorTextDarker = "errorTextDarker".attr(),
          errorTextMenu = "errorTextMenu".attr(),
          errorBorder = "errorBorder".attr(),
          upcomingBackground = "upcomingBackground".attr(),
          upcomingText = "upcomingText".attr(),
          upcomingBorder = "upcomingBorder".attr(),
        ),
      form =
        FormColors(
          formLabelText = "formLabelText".attr(),
          formLabelBackground = "formLabelBackground".attr(),
          formInputBackground = "formInputBackground".attr(),
          formInputBackgroundSelected = "formInputBackgroundSelected".attr(),
          formInputBackgroundSelection = "formInputBackgroundSelection".attr(),
          formInputBorder = "formInputBorder".attr(),
          formInputTextReadOnlySelection = "formInputTextReadOnlySelection".attr(),
          formInputBorderSelected = "formInputBorderSelected".attr(),
          formInputText = "formInputText".attr(),
          formInputTextSelected = "formInputTextSelected".attr(),
          formInputTextPlaceholder = "formInputTextPlaceholder".attr(),
          formInputTextPlaceholderSelected = "formInputTextPlaceholderSelected".attr(),
          formInputTextSelection = "formInputTextSelection".attr(),
          formInputShadowSelected = "formInputShadowSelected".attr(),
          formInputTextHighlight = "formInputTextHighlight".attr(),
          checkboxText = "checkboxText".attr(),
          checkboxBackgroundSelected = "checkboxBackgroundSelected".attr(),
          checkboxBorderSelected = "checkboxBorderSelected".attr(),
          checkboxShadowSelected = "checkboxShadowSelected".attr(),
          checkboxToggleBackground = "checkboxToggleBackground".attr(),
          checkboxToggleBackgroundSelected = "checkboxToggleBackgroundSelected".attr(),
          checkboxToggleDisabled = "checkboxToggleDisabled".attr(),
        ),
      misc =
        MiscColors(
          pillBackground = "pillBackground".attr(),
          pillBackgroundLight = "pillBackgroundLight".attr(),
          pillText = "pillText".attr(),
          pillTextHighlighted = "pillTextHighlighted".attr(),
          pillBorder = "pillBorder".attr(),
          pillBorderDark = "pillBorderDark".attr(),
          pillBackgroundSelected = "pillBackgroundSelected".attr(),
          pillTextSelected = "pillTextSelected".attr(),
          pillBorderSelected = "pillBorderSelected".attr(),
          pillTextSubdued = "pillTextSubdued".attr(),
          reportsRed = "reportsRed".attr(),
          reportsBlue = "reportsBlue".attr(),
          reportsGreen = "reportsGreen".attr(),
          reportsGray = "reportsGray".attr(),
          reportsLabel = "reportsLabel".attr(),
          reportsInnerLabel = "reportsInnerLabel".attr(),
          reportsNumberPositive = "reportsNumberPositive".attr(),
          reportsNumberNegative = "reportsNumberNegative".attr(),
          reportsNumberNeutral = "reportsNumberNeutral".attr(),
          reportsChartFill = "reportsChartFill".attr(),
          noteTagBackground = "noteTagBackground".attr(),
          noteTagBackgroundHover = "noteTagBackgroundHover".attr(),
          noteTagDefault = "noteTagDefault".attr(),
          noteTagText = "noteTagText".attr(),
          budgetCurrentMonth = "budgetCurrentMonth".attr(),
          budgetOtherMonth = "budgetOtherMonth".attr(),
          budgetHeaderCurrentMonth = "budgetHeaderCurrentMonth".attr(),
          budgetHeaderOtherMonth = "budgetHeaderOtherMonth".attr(),
          budgetNumberZero = "budgetNumberZero".attr(),
          budgetNumberNegative = "budgetNumberNegative".attr(),
          budgetNumberNeutral = "budgetNumberNeutral".attr(),
          budgetNumberPositive = "budgetNumberPositive".attr(),
          templateNumberFunded = "templateNumberFunded".attr(),
          templateNumberUnderFunded = "templateNumberUnderFunded".attr(),
          toBudgetPositive = "toBudgetPositive".attr(),
          toBudgetZero = "toBudgetZero".attr(),
          toBudgetNegative = "toBudgetNegative".attr(),
          floatingActionBarBackground = "floatingActionBarBackground".attr(),
          floatingActionBarBorder = "floatingActionBarBorder".attr(),
          floatingActionBarText = "floatingActionBarText".attr(),
          tooltipText = "tooltipText".attr(),
          tooltipBackground = "tooltipBackground".attr(),
          tooltipBorder = "tooltipBorder".attr(),
          overlayBackground = "overlayBackground".attr(),
        ),
    )

  if (attributes.isNotEmpty()) {
    logcat.w { "Parsed custom theme, leftover attributes = $attributes" }
  } else {
    logcat.d { "Parsed custom theme, no leftover attributes" }
  }

  return theme
}

private val AttributeRegex = "--color-(.*?): (.*?);".toRegex()

private fun parseAttributePair(line: String): Pair<String, Color> {
  val match =
    AttributeRegex.find(line) ?: error("Attribute '$line' doesn't match regex $AttributeRegex")
  val (_, name, string) = match.groupValues
  return name to string.parseColor()
}
