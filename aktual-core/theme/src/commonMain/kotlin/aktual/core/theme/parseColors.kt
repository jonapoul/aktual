@file:Suppress("LongMethod")

package aktual.core.theme

import androidx.compose.ui.graphics.Color
import logcat.logcat

fun parseColors(summary: CustomThemeSummary, css: String): CustomColors {
  val properties: Map<String, String> =
    css
      .lineSequence()
      .map { it.trim() }
      .filter { it.startsWith("--") }
      .mapNotNull(::parsePropertyPair)
      .toMap()

  val attributes = mutableMapOf<String, Color>()
  for ((property, value) in properties) {
    if (!property.startsWith(COLOR_PREFIX)) continue
    val name = property.removePrefix(COLOR_PREFIX)
    val color = properties.resolveColor(value)
    if (color == null) {
      logcat.w(TAG) { "Can't resolve '$value' for '$name' in custom theme ${summary.repo}" }
    } else {
      attributes[name] = color
    }
  }

  val pageBackground =
    requireNotNull(attributes.remove("pageBackground")) {
      "Key 'pageBackground' not found in CSS attributes: $attributes"
    }

  val fallbackTheme = if (pageBackground.isLight()) LightColors else DarkColors

  fun String.attr(fallback: Color): Color =
    attributes.remove(this)
      ?: run {
        logcat.w(TAG) { "Failed finding theme attribute '$this' in custom theme ${summary.repo}" }
        fallback
      }

  // A theme without its own alternate row colour leaves rows unshaded, as the built-in themes do
  val tableBackground = "tableBackground".attr(fallbackTheme.tableBackground)

  val theme =
    with(fallbackTheme) {
      JsonCustomColors(
        name = summary.name,
        repo = summary.repo,
        isLight = summary.mode == Light,
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
            tableBackground = tableBackground,
            tableRowBackgroundAlternate = "tableRowBackgroundAlternate".attr(tableBackground),
            tableRowBackgroundHover = "tableRowBackgroundHover".attr(tableRowBackgroundHover),
            tableText = "tableText".attr(tableText),
            tableTextItemAdded = "tableTextItemAdded".attr(tableTextItemAdded),
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
            sidebarItemTextUpdated = "sidebarItemTextUpdated".attr(sidebarItemTextUpdated),
            sidebarItemTextSelected = "sidebarItemTextSelected".attr(sidebarItemTextSelected),
            sidebarBudgetName = "sidebarBudgetName".attr(sidebarBudgetName),
            sidebarItemBackgroundSelected =
              "sidebarItemBackgroundSelected".attr(sidebarItemBackgroundSelected),
            sidebarHeaderText = "sidebarHeaderText".attr(sidebarHeaderText),
            sidebarTextSubdued = "sidebarTextSubdued".attr(sidebarTextSubdued),
            sidebarTextMuted = "sidebarTextMuted".attr(sidebarTextMuted),
            sidebarTextPositive = "sidebarTextPositive".attr(sidebarTextPositive),
            sidebarTextFailed = "sidebarTextFailed".attr(sidebarTextFailed),
            sidebarBackgroundFailedSubtle =
              "sidebarBackgroundFailedSubtle".attr(sidebarBackgroundFailedSubtle),
            sidebarBorder = "sidebarBorder".attr(sidebarBorder),
            sidebarControlBackground = "sidebarControlBackground".attr(sidebarControlBackground),
            sidebarBrand = "sidebarBrand".attr(sidebarBrand),
            sidebarRedesignBackground = "sidebarRedesignBackground".attr(sidebarRedesignBackground),
            sidebarRedesignItemText = "sidebarRedesignItemText".attr(sidebarRedesignItemText),
            sidebarRedesignItemBackgroundHover =
              "sidebarRedesignItemBackgroundHover".attr(sidebarRedesignItemBackgroundHover),
            sidebarRedesignItemAccentSelected =
              "sidebarRedesignItemAccentSelected".attr(sidebarRedesignItemAccentSelected),
            sidebarRedesignItemTextSelected =
              "sidebarRedesignItemTextSelected".attr(sidebarRedesignItemTextSelected),
            sidebarRedesignItemBackgroundSelected =
              "sidebarRedesignItemBackgroundSelected".attr(sidebarRedesignItemBackgroundSelected),
            sidebarRedesignItemBackgroundFailed =
              "sidebarRedesignItemBackgroundFailed".attr(sidebarRedesignItemBackgroundFailed),
            sidebarRedesignHeaderText = "sidebarRedesignHeaderText".attr(sidebarRedesignHeaderText),
            sidebarRedesignTextSubdued =
              "sidebarRedesignTextSubdued".attr(sidebarRedesignTextSubdued),
            sidebarRedesignTextMuted = "sidebarRedesignTextMuted".attr(sidebarRedesignTextMuted),
            sidebarRedesignRightBorder =
              "sidebarRedesignRightBorder".attr(sidebarRedesignRightBorder),
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
            datePickerRangeBackground = "datePickerRangeBackground".attr(datePickerRangeBackground),
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
            noticeTextMenuHover = "noticeTextMenuHover".attr(noticeTextMenuHover),
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
        chartQual =
          ChartQualColors(
            chartQual1 = "chartQual1".attr(chartQual1),
            chartQual2 = "chartQual2".attr(chartQual2),
            chartQual3 = "chartQual3".attr(chartQual3),
            chartQual4 = "chartQual4".attr(chartQual4),
            chartQual5 = "chartQual5".attr(chartQual5),
            chartQual6 = "chartQual6".attr(chartQual6),
            chartQual7 = "chartQual7".attr(chartQual7),
            chartQual8 = "chartQual8".attr(chartQual8),
            chartQual9 = "chartQual9".attr(chartQual9),
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

private const val COLOR_PREFIX = "color-"

private val PropertyRegex = "--(.*?):\\s*?(.*?);".toRegex()

private val VarRegex = """var\(\s*--([\w-]+)\s*(?:,\s*(.+?)\s*)?\)""".toRegex()

// Themes can declare their own properties alongside the --color- ones, and reference them by var()
private fun parsePropertyPair(line: String): Pair<String, String>? {
  val match = PropertyRegex.find(line)
  if (match == null) {
    check(!line.startsWith("--$COLOR_PREFIX")) {
      "Attribute '$line' doesn't match regex $PropertyRegex"
    }
    return null
  }
  val (_, name, string) = match.groupValues
  return name to string.trim()
}

// Follows var() references to other properties, returning null if one is missing or circular
private fun Map<String, String>.resolveColor(
  value: String,
  seen: Set<String> = emptySet(),
): Color? {
  val match = VarRegex.matchEntire(value) ?: return value.parseColor()
  val (_, name, fallback) = match.groupValues
  val target = if (name in seen) null else get(name)
  return target?.let { resolveColor(it, seen + name) }
    ?: fallback.takeIf { it.isNotEmpty() }?.let { resolveColor(it, seen) }
}
