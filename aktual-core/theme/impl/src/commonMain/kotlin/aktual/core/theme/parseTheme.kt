@file:Suppress("LongMethod")

package aktual.core.theme

import androidx.compose.ui.graphics.Color
import logcat.logcat

internal fun parseTheme(summary: CustomThemeSummary, css: String): CustomTheme {
  val attributes: MutableMap<String, Color> =
    css
      .lineSequence()
      .map { it.trim() }
      .filter { it.startsWith("--color-") }
      .associate(::parseAttributePair)
      .toMutableMap()

  val pageBackground =
    requireNotNull(attributes.remove("pageBackground")) {
      "Key 'pageBackground' not found in CSS attributes: $attributes"
    }

  val fallbackTheme = if (pageBackground.isLight()) LightTheme else DarkTheme

  fun String.attr(fallback: Color): Color =
    attributes.remove(this)
      ?: run {
        logcat.w(TAG) { "Failed finding theme attribute '$this' in custom theme ${summary.repo}" }
        fallback
      }

  val theme =
    with(fallbackTheme) {
      JsonCustomTheme(
        name = summary.name,
        repo = summary.repo,
        isLight = summary.mode == ThemeMode.Light,
        page =
          PageColors(
            pageBackground = pageBackground,
            pageBackgroundModalActive = "pageBackgroundModalActive".attr(pageBackgroundModalActive),
            pageBackgroundTopLeft = "pageBackgroundTopLeft".attr(pageBackgroundTopLeft),
            pageBackgroundBottomRight = "pageBackgroundBottomRight".attr(pageBackgroundBottomRight),
            pageBackgroundLineTop = "pageBackgroundLineTop".attr(pageBackgroundLineTop),
            pageBackgroundLineMid = "pageBackgroundLineMid".attr(pageBackgroundLineMid),
            pageBackgroundLineBottom = "pageBackgroundLineBottom".attr(pageBackgroundLineBottom),
            pageText = "pageText".attr(pageText),
            pageTextLight = "pageTextLight".attr(pageTextLight),
            pageTextSubdued = "pageTextSubdued".attr(pageTextSubdued),
            pageTextDark = "pageTextDark".attr(pageTextDark),
            pageTextPositive = "pageTextPositive".attr(pageTextPositive),
            pageTextLink = "pageTextLink".attr(pageTextLink),
            pageTextLinkLight = "pageTextLinkLight".attr(pageTextLinkLight),
            numberPositive = "numberPositive".attr(numberPositive),
            numberNegative = "numberNegative".attr(numberNegative),
            numberNeutral = "numberNeutral".attr(numberNeutral),
            cardBackground = "cardBackground".attr(cardBackground),
            cardBorder = "cardBorder".attr(cardBorder),
            cardShadow = "cardShadow".attr(cardShadow),
          ),
        table =
          TableColors(
            tableBackground = "tableBackground".attr(tableBackground),
            tableRowBackgroundHover = "tableRowBackgroundHover".attr(tableRowBackgroundHover),
            tableText = "tableText".attr(tableText),
            tableTextLight = "tableTextLight".attr(tableTextLight),
            tableTextSubdued = "tableTextSubdued".attr(tableTextSubdued),
            tableTextSelected = "tableTextSelected".attr(tableTextSelected),
            tableTextHover = "tableTextHover".attr(tableTextHover),
            tableTextInactive = "tableTextInactive".attr(tableTextInactive),
            tableHeaderText = "tableHeaderText".attr(tableHeaderText),
            tableHeaderBackground = "tableHeaderBackground".attr(tableHeaderBackground),
            tableBorder = "tableBorder".attr(tableBorder),
            tableBorderSelected = "tableBorderSelected".attr(tableBorderSelected),
            tableBorderHover = "tableBorderHover".attr(tableBorderHover),
            tableBorderSeparator = "tableBorderSeparator".attr(tableBorderSeparator),
            tableRowBackgroundHighlight =
              "tableRowBackgroundHighlight".attr(tableRowBackgroundHighlight),
            tableRowBackgroundHighlightText =
              "tableRowBackgroundHighlightText".attr(tableRowBackgroundHighlightText),
            tableRowHeaderBackground = "tableRowHeaderBackground".attr(tableRowHeaderBackground),
            tableRowHeaderText = "tableRowHeaderText".attr(tableRowHeaderText),
          ),
        navigation =
          NavigationColors(
            sidebarBackground = "sidebarBackground".attr(sidebarBackground),
            sidebarItemBackgroundPending =
              "sidebarItemBackgroundPending".attr(sidebarItemBackgroundPending),
            sidebarItemBackgroundPositive =
              "sidebarItemBackgroundPositive".attr(sidebarItemBackgroundPositive),
            sidebarItemBackgroundFailed =
              "sidebarItemBackgroundFailed".attr(sidebarItemBackgroundFailed),
            sidebarItemAccentSelected = "sidebarItemAccentSelected".attr(sidebarItemAccentSelected),
            sidebarItemBackgroundHover =
              "sidebarItemBackgroundHover".attr(sidebarItemBackgroundHover),
            sidebarItemText = "sidebarItemText".attr(sidebarItemText),
            sidebarItemTextSelected = "sidebarItemTextSelected".attr(sidebarItemTextSelected),
            sidebarBudgetName = "sidebarBudgetName".attr(sidebarBudgetName),
            menuBackground = "menuBackground".attr(menuBackground),
            menuItemBackground = "menuItemBackground".attr(menuItemBackground),
            menuItemBackgroundHover = "menuItemBackgroundHover".attr(menuItemBackgroundHover),
            menuItemText = "menuItemText".attr(menuItemText),
            menuItemTextHover = "menuItemTextHover".attr(menuItemTextHover),
            menuItemTextSelected = "menuItemTextSelected".attr(menuItemTextSelected),
            menuItemTextHeader = "menuItemTextHeader".attr(menuItemTextHeader),
            menuBorder = "menuBorder".attr(menuBorder),
            menuBorderHover = "menuBorderHover".attr(menuBorderHover),
            menuKeybindingText = "menuKeybindingText".attr(menuKeybindingText),
            menuAutoCompleteBackground =
              "menuAutoCompleteBackground".attr(menuAutoCompleteBackground),
            menuAutoCompleteBackgroundHover =
              "menuAutoCompleteBackgroundHover".attr(menuAutoCompleteBackgroundHover),
            menuAutoCompleteText = "menuAutoCompleteText".attr(menuAutoCompleteText),
            menuAutoCompleteTextHover = "menuAutoCompleteTextHover".attr(menuAutoCompleteTextHover),
            menuAutoCompleteTextHeader =
              "menuAutoCompleteTextHeader".attr(menuAutoCompleteTextHeader),
            menuAutoCompleteItemTextHover =
              "menuAutoCompleteItemTextHover".attr(menuAutoCompleteItemTextHover),
            menuAutoCompleteItemText = "menuAutoCompleteItemText".attr(menuAutoCompleteItemText),
          ),
        modalMobile =
          ModalMobileColors(
            modalBackground = "modalBackground".attr(modalBackground),
            modalBorder = "modalBorder".attr(modalBorder),
            mobileHeaderBackground = "mobileHeaderBackground".attr(mobileHeaderBackground),
            mobileHeaderText = "mobileHeaderText".attr(mobileHeaderText),
            mobileHeaderTextSubdued = "mobileHeaderTextSubdued".attr(mobileHeaderTextSubdued),
            mobileHeaderTextHover = "mobileHeaderTextHover".attr(mobileHeaderTextHover),
            mobilePageBackground = "mobilePageBackground".attr(mobilePageBackground),
            mobileNavBackground = "mobileNavBackground".attr(mobileNavBackground),
            mobileNavItem = "mobileNavItem".attr(mobileNavItem),
            mobileNavItemSelected = "mobileNavItemSelected".attr(mobileNavItemSelected),
            mobileAccountShadow = "mobileAccountShadow".attr(mobileAccountShadow),
            mobileAccountText = "mobileAccountText".attr(mobileAccountText),
            mobileTransactionSelected = "mobileTransactionSelected".attr(mobileTransactionSelected),
            mobileViewTheme = "mobileViewTheme".attr(mobileViewTheme),
            mobileConfigServerViewTheme =
              "mobileConfigServerViewTheme".attr(mobileConfigServerViewTheme),
            markdownNormal = "markdownNormal".attr(markdownNormal),
            markdownDark = "markdownDark".attr(markdownDark),
            markdownLight = "markdownLight".attr(markdownLight),
          ),
        button =
          ButtonColors(
            buttonMenuText = "buttonMenuText".attr(buttonMenuText),
            buttonMenuTextHover = "buttonMenuTextHover".attr(buttonMenuTextHover),
            buttonMenuBackground = "buttonMenuBackground".attr(buttonMenuBackground),
            buttonMenuBackgroundHover = "buttonMenuBackgroundHover".attr(buttonMenuBackgroundHover),
            buttonMenuBorder = "buttonMenuBorder".attr(buttonMenuBorder),
            buttonMenuSelectedText = "buttonMenuSelectedText".attr(buttonMenuSelectedText),
            buttonMenuSelectedTextHover =
              "buttonMenuSelectedTextHover".attr(buttonMenuSelectedTextHover),
            buttonMenuSelectedBackground =
              "buttonMenuSelectedBackground".attr(buttonMenuSelectedBackground),
            buttonMenuSelectedBackgroundHover =
              "buttonMenuSelectedBackgroundHover".attr(buttonMenuSelectedBackgroundHover),
            buttonMenuSelectedBorder = "buttonMenuSelectedBorder".attr(buttonMenuSelectedBorder),
            buttonPrimaryText = "buttonPrimaryText".attr(buttonPrimaryText),
            buttonPrimaryTextHover = "buttonPrimaryTextHover".attr(buttonPrimaryTextHover),
            buttonPrimaryBackground = "buttonPrimaryBackground".attr(buttonPrimaryBackground),
            buttonPrimaryBackgroundHover =
              "buttonPrimaryBackgroundHover".attr(buttonPrimaryBackgroundHover),
            buttonPrimaryBorder = "buttonPrimaryBorder".attr(buttonPrimaryBorder),
            buttonPrimaryShadow = "buttonPrimaryShadow".attr(buttonPrimaryShadow),
            buttonPrimaryDisabledText = "buttonPrimaryDisabledText".attr(buttonPrimaryDisabledText),
            buttonPrimaryDisabledBackground =
              "buttonPrimaryDisabledBackground".attr(buttonPrimaryDisabledBackground),
            buttonPrimaryDisabledBorder =
              "buttonPrimaryDisabledBorder".attr(buttonPrimaryDisabledBorder),
            buttonNormalText = "buttonNormalText".attr(buttonNormalText),
            buttonNormalTextHover = "buttonNormalTextHover".attr(buttonNormalTextHover),
            buttonNormalBackground = "buttonNormalBackground".attr(buttonNormalBackground),
            buttonNormalBackgroundHover =
              "buttonNormalBackgroundHover".attr(buttonNormalBackgroundHover),
            buttonNormalBorder = "buttonNormalBorder".attr(buttonNormalBorder),
            buttonNormalShadow = "buttonNormalShadow".attr(buttonNormalShadow),
            buttonNormalSelectedText = "buttonNormalSelectedText".attr(buttonNormalSelectedText),
            buttonNormalSelectedBackground =
              "buttonNormalSelectedBackground".attr(buttonNormalSelectedBackground),
            buttonNormalDisabledText = "buttonNormalDisabledText".attr(buttonNormalDisabledText),
            buttonNormalDisabledBackground =
              "buttonNormalDisabledBackground".attr(buttonNormalDisabledBackground),
            buttonNormalDisabledBorder =
              "buttonNormalDisabledBorder".attr(buttonNormalDisabledBorder),
            calendarText = "calendarText".attr(calendarText),
            calendarBackground = "calendarBackground".attr(calendarBackground),
            calendarItemText = "calendarItemText".attr(calendarItemText),
            calendarItemBackground = "calendarItemBackground".attr(calendarItemBackground),
            calendarSelectedBackground =
              "calendarSelectedBackground".attr(calendarSelectedBackground),
            calendarCellBackground = "calendarCellBackground".attr(calendarCellBackground),
            buttonBareText = "buttonBareText".attr(buttonBareText),
            buttonBareTextHover = "buttonBareTextHover".attr(buttonBareTextHover),
            buttonBareBackground = "buttonBareBackground".attr(buttonBareBackground),
            buttonBareBackgroundHover = "buttonBareBackgroundHover".attr(buttonBareBackgroundHover),
            buttonBareBackgroundActive =
              "buttonBareBackgroundActive".attr(buttonBareBackgroundActive),
            buttonBareDisabledText = "buttonBareDisabledText".attr(buttonBareDisabledText),
            buttonBareDisabledBackground =
              "buttonBareDisabledBackground".attr(buttonBareDisabledBackground),
          ),
        status =
          StatusColors(
            noticeBackground = "noticeBackground".attr(noticeBackground),
            noticeBackgroundLight = "noticeBackgroundLight".attr(noticeBackgroundLight),
            noticeBackgroundDark = "noticeBackgroundDark".attr(noticeBackgroundDark),
            noticeText = "noticeText".attr(noticeText),
            noticeTextLight = "noticeTextLight".attr(noticeTextLight),
            noticeTextDark = "noticeTextDark".attr(noticeTextDark),
            noticeTextMenu = "noticeTextMenu".attr(noticeTextMenu),
            noticeBorder = "noticeBorder".attr(noticeBorder),
            warningBackground = "warningBackground".attr(warningBackground),
            warningText = "warningText".attr(warningText),
            warningTextLight = "warningTextLight".attr(warningTextLight),
            warningTextDark = "warningTextDark".attr(warningTextDark),
            warningBorder = "warningBorder".attr(warningBorder),
            errorBackground = "errorBackground".attr(errorBackground),
            errorText = "errorText".attr(errorText),
            errorTextDark = "errorTextDark".attr(errorTextDark),
            errorTextDarker = "errorTextDarker".attr(errorTextDarker),
            errorTextMenu = "errorTextMenu".attr(errorTextMenu),
            errorBorder = "errorBorder".attr(errorBorder),
            upcomingBackground = "upcomingBackground".attr(upcomingBackground),
            upcomingText = "upcomingText".attr(upcomingText),
            upcomingBorder = "upcomingBorder".attr(upcomingBorder),
          ),
        form =
          FormColors(
            formLabelText = "formLabelText".attr(formLabelText),
            formLabelBackground = "formLabelBackground".attr(formLabelBackground),
            formInputBackground = "formInputBackground".attr(formInputBackground),
            formInputBackgroundSelected =
              "formInputBackgroundSelected".attr(formInputBackgroundSelected),
            formInputBackgroundSelection =
              "formInputBackgroundSelection".attr(formInputBackgroundSelection),
            formInputBorder = "formInputBorder".attr(formInputBorder),
            formInputTextReadOnlySelection =
              "formInputTextReadOnlySelection".attr(formInputTextReadOnlySelection),
            formInputBorderSelected = "formInputBorderSelected".attr(formInputBorderSelected),
            formInputText = "formInputText".attr(formInputText),
            formInputTextSelected = "formInputTextSelected".attr(formInputTextSelected),
            formInputTextPlaceholder = "formInputTextPlaceholder".attr(formInputTextPlaceholder),
            formInputTextPlaceholderSelected =
              "formInputTextPlaceholderSelected".attr(formInputTextPlaceholderSelected),
            formInputTextSelection = "formInputTextSelection".attr(formInputTextSelection),
            formInputShadowSelected = "formInputShadowSelected".attr(formInputShadowSelected),
            formInputTextHighlight = "formInputTextHighlight".attr(formInputTextHighlight),
            checkboxText = "checkboxText".attr(checkboxText),
            checkboxBackgroundSelected =
              "checkboxBackgroundSelected".attr(checkboxBackgroundSelected),
            checkboxBorderSelected = "checkboxBorderSelected".attr(checkboxBorderSelected),
            checkboxShadowSelected = "checkboxShadowSelected".attr(checkboxShadowSelected),
            checkboxToggleBackground = "checkboxToggleBackground".attr(checkboxToggleBackground),
            checkboxToggleBackgroundSelected =
              "checkboxToggleBackgroundSelected".attr(checkboxToggleBackgroundSelected),
            checkboxToggleDisabled = "checkboxToggleDisabled".attr(checkboxToggleDisabled),
          ),
        misc =
          MiscColors(
            pillBackground = "pillBackground".attr(pillBackground),
            pillBackgroundLight = "pillBackgroundLight".attr(pillBackgroundLight),
            pillText = "pillText".attr(pillText),
            pillTextHighlighted = "pillTextHighlighted".attr(pillTextHighlighted),
            pillBorder = "pillBorder".attr(pillBorder),
            pillBorderDark = "pillBorderDark".attr(pillBorderDark),
            pillBackgroundSelected = "pillBackgroundSelected".attr(pillBackgroundSelected),
            pillTextSelected = "pillTextSelected".attr(pillTextSelected),
            pillBorderSelected = "pillBorderSelected".attr(pillBorderSelected),
            pillTextSubdued = "pillTextSubdued".attr(pillTextSubdued),
            reportsRed = "reportsRed".attr(reportsRed),
            reportsBlue = "reportsBlue".attr(reportsBlue),
            reportsGreen = "reportsGreen".attr(reportsGreen),
            reportsGray = "reportsGray".attr(reportsGray),
            reportsLabel = "reportsLabel".attr(reportsLabel),
            reportsInnerLabel = "reportsInnerLabel".attr(reportsInnerLabel),
            reportsNumberPositive = "reportsNumberPositive".attr(reportsNumberPositive),
            reportsNumberNegative = "reportsNumberNegative".attr(reportsNumberNegative),
            reportsNumberNeutral = "reportsNumberNeutral".attr(reportsNumberNeutral),
            reportsChartFill = "reportsChartFill".attr(reportsChartFill),
            noteTagBackground = "noteTagBackground".attr(noteTagBackground),
            noteTagBackgroundHover = "noteTagBackgroundHover".attr(noteTagBackgroundHover),
            noteTagDefault = "noteTagDefault".attr(noteTagDefault),
            noteTagText = "noteTagText".attr(noteTagText),
            budgetCurrentMonth = "budgetCurrentMonth".attr(budgetCurrentMonth),
            budgetOtherMonth = "budgetOtherMonth".attr(budgetOtherMonth),
            budgetHeaderCurrentMonth = "budgetHeaderCurrentMonth".attr(budgetHeaderCurrentMonth),
            budgetHeaderOtherMonth = "budgetHeaderOtherMonth".attr(budgetHeaderOtherMonth),
            budgetNumberZero = "budgetNumberZero".attr(budgetNumberZero),
            budgetNumberNegative = "budgetNumberNegative".attr(budgetNumberNegative),
            budgetNumberNeutral = "budgetNumberNeutral".attr(budgetNumberNeutral),
            budgetNumberPositive = "budgetNumberPositive".attr(budgetNumberPositive),
            templateNumberFunded = "templateNumberFunded".attr(templateNumberFunded),
            templateNumberUnderFunded = "templateNumberUnderFunded".attr(templateNumberUnderFunded),
            toBudgetPositive = "toBudgetPositive".attr(toBudgetPositive),
            toBudgetZero = "toBudgetZero".attr(toBudgetZero),
            toBudgetNegative = "toBudgetNegative".attr(toBudgetNegative),
            floatingActionBarBackground =
              "floatingActionBarBackground".attr(floatingActionBarBackground),
            floatingActionBarBorder = "floatingActionBarBorder".attr(floatingActionBarBorder),
            floatingActionBarText = "floatingActionBarText".attr(floatingActionBarText),
            tooltipText = "tooltipText".attr(tooltipText),
            tooltipBackground = "tooltipBackground".attr(tooltipBackground),
            tooltipBorder = "tooltipBorder".attr(tooltipBorder),
            overlayBackground = "overlayBackground".attr(overlayBackground),
          ),
      )
    }

  if (attributes.isNotEmpty()) {
    logcat.w(TAG) { "Parsed ${summary.repo}, leftover attributes = $attributes" }
  } else {
    logcat.d(TAG) { "Parsed ${summary.repo}, no leftover attributes" }
  }

  return theme
}

private const val TAG = "parseTheme"

private val AttributeRegex = "--color-(.*?):\\s*?(.*?);".toRegex()

private fun parseAttributePair(line: String): Pair<String, Color> {
  val match =
    AttributeRegex.find(line) ?: error("Attribute '$line' doesn't match regex $AttributeRegex")
  val (_, name, string) = match.groupValues
  return name to string.trim().parseColor()
}
