@file:Suppress("LongMethod")

package aktual.theme

import aktual.log
import androidx.compose.ui.graphics.Color

fun parseColors(summary: CustomThemeSummary, css: String): CustomColors {
  val properties: Map<String, String> =
    css
      .replace(CommentRegex, "")
      .split(DeclarationSeparator)
      .asSequence()
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
      log.w(TAG) { "Can't resolve '$value' for '$name' in custom theme ${summary.repo}" }
    } else {
      attributes[name] = color
    }
  }

  val pageBackground =
    requireNotNull(attributes["pageBackground"] ?: attributes["backgroundPage"]) {
      "Neither 'pageBackground' nor 'backgroundPage' found in CSS attributes: $attributes"
    }

  val theme =
    buildCustomColors(
      name = summary.name,
      repo = summary.repo,
      isLight = summary.mode == Light,
      fallback = if (pageBackground.isLight()) LightColors else DarkColors,
      attributes = attributes,
      onMissing = { name ->
        log.w(TAG) { "Failed finding theme attribute '$name' in custom theme ${summary.repo}" }
      },
    )

  if (attributes.isNotEmpty()) {
    log.w(TAG) { "Parsed ${summary.repo}, leftover attributes = $attributes" }
  } else {
    log.d(TAG) { "Parsed ${summary.repo}, no leftover attributes" }
  }

  return theme
}

/**
 * Copies this theme as [name] under [repo], with [overrides] replacing any of its [attributes].
 * Throws if an override doesn't name a known attribute.
 */
fun Colors.toCustomColors(
  name: String,
  repo: CustomThemeRepo,
  overrides: Map<String, Color> = emptyMap(),
): JsonCustomColors {
  val current = attributes()
  val unknown = overrides.keys - current.keys
  require(unknown.isEmpty()) { "Unknown theme attributes $unknown" }
  return buildCustomColors(
    name = name,
    repo = repo,
    isLight = isLight,
    fallback = this,
    attributes = (current + overrides).toMutableMap(),
    onMissing = { attribute -> error("Missing attribute '$attribute' from $id") },
  )
}

// Removes each attribute from [attributes] as it's used, so the caller can tell what's left over
private fun buildCustomColors(
  name: String,
  repo: CustomThemeRepo,
  isLight: Boolean,
  fallback: Colors,
  attributes: MutableMap<String, Color>,
  onMissing: (String) -> Unit,
): JsonCustomColors {
  fun String.attr(default: Color): Color =
    attributes.remove(this)
      ?: run {
        onMissing(this)
        default
      }

  // Themes from before upstream's accent colours set the colours that used to play their part, so
  // take the accent from those. Colours a theme leaves out then follow its accent, as upstream's do
  fun String.attrOr(legacy: String, default: Color): Color = attr(attributes[legacy] ?: default)

  val accent = "accent".attrOr("buttonPrimaryBackground", fallback.accent)
  val accentContrast = "accentContrast".attrOr("buttonPrimaryText", fallback.accentContrast)
  val accentHover = "accentHover".attrOr("buttonPrimaryBackgroundHover", fallback.accentHover)
  val accentText = "accentText".attrOr("pageTextLink", fallback.accentText)
  val accentBorder = "accentBorder".attrOr("formInputBorderSelected", fallback.accentBorder)
  val accentSubtle = "accentSubtle".attrOr("tableRowBackgroundHighlight", fallback.accentSubtle)
  val accentSubtleText = "accentSubtleText".attrOr("pillTextSelected", fallback.accentSubtleText)

  // Likewise the base colours, taken from the colours that now follow them
  val backgroundPage = "backgroundPage".attrOr("pageBackground", fallback.backgroundPage)
  val background = "background".attrOr("tableBackground", fallback.background)
  val backgroundRaised = "backgroundRaised".attrOr("menuBackground", fallback.backgroundRaised)
  val backgroundHover =
    "backgroundHover".attrOr("tableRowBackgroundHover", fallback.backgroundHover)
  val backgroundSelected = "backgroundSelected".attrOr("tableRowBackgroundHighlight", accentSubtle)
  val border = "border".attrOr("tableBorder", fallback.border)
  val borderStrong = "borderStrong".attrOr("tableBorderSeparator", fallback.borderStrong)
  val text = "text".attrOr("pageText", fallback.text)
  val textSecondary = "textSecondary".attrOr("pageTextLight", fallback.textSecondary)
  val textTertiary = "textTertiary".attrOr("pageTextSubdued", fallback.textTertiary)

  // A theme without its own alternate row colour leaves rows unshaded, as the built-in themes do
  val tableBackground = "tableBackground".attr(background)

  val themeName = name
  val themeRepo = repo
  val themeIsLight = isLight
  return with(fallback) {
    JsonCustomColors(
      name = themeName,
      repo = themeRepo,
      isLight = themeIsLight,
      page =
        PageColors(
          pageBackground = "pageBackground".attr(backgroundPage),
          pageBackgroundTopLeft = "pageBackgroundTopLeft".attr(pageBackgroundTopLeft),
          pageBackgroundBottomRight = "pageBackgroundBottomRight".attr(pageBackgroundBottomRight),
          pageBackgroundLineTop = "pageBackgroundLineTop".attr(pageBackgroundLineTop),
          pageBackgroundLineMid = "pageBackgroundLineMid".attr(pageBackgroundLineMid),
          pageBackgroundLineBottom = "pageBackgroundLineBottom".attr(pageBackgroundLineBottom),
          pageText = "pageText".attr(text),
          pageTextLight = "pageTextLight".attr(textSecondary),
          pageTextSubdued = "pageTextSubdued".attr(textTertiary),
          pageTextDark = "pageTextDark".attr(text),
          pageTextPositive = "pageTextPositive".attr(accentText),
          pageTextLink = "pageTextLink".attr(accentText),
          pageTextLinkLight = "pageTextLinkLight".attr(accentText),
          numberPositive = "numberPositive".attr(numberPositive),
          numberNegative = "numberNegative".attr(numberNegative),
          numberNeutral = "numberNeutral".attr(textSecondary),
          cardBackground = "cardBackground".attr(background),
          cardBorder = "cardBorder".attr(accentBorder),
          cardShadow = "cardShadow".attr(cardShadow),
        ),
      table =
        TableColors(
          tableBackground = tableBackground,
          tableRowBackgroundAlternate = "tableRowBackgroundAlternate".attr(tableBackground),
          tableRowBackgroundHover = "tableRowBackgroundHover".attr(backgroundHover),
          tableText = "tableText".attr(text),
          tableTextItemAdded = "tableTextItemAdded".attr(tableTextItemAdded),
          tableTextLight = "tableTextLight".attr(tableTextLight),
          tableTextSubdued = "tableTextSubdued".attr(textSecondary),
          tableTextSelected = "tableTextSelected".attr(tableTextSelected),
          tableTextInactive = "tableTextInactive".attr(tableTextInactive),
          tableHeaderText = "tableHeaderText".attr(tableHeaderText),
          tableHeaderBackground = "tableHeaderBackground".attr(tableHeaderBackground),
          tableBorder = "tableBorder".attr(border),
          tableBorderSelected = "tableBorderSelected".attr(accentBorder),
          tableBorderHover = "tableBorderHover".attr(accentBorder),
          tableBorderSeparator = "tableBorderSeparator".attr(borderStrong),
          tableRowBackgroundHighlight = "tableRowBackgroundHighlight".attr(backgroundSelected),
          tableRowBackgroundHighlightText =
            "tableRowBackgroundHighlightText".attr(tableRowBackgroundHighlightText),
          tableRowHeaderBackground = "tableRowHeaderBackground".attr(tableRowHeaderBackground),
          tableRowHeaderText = "tableRowHeaderText".attr(text),
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
          menuBackground = "menuBackground".attr(backgroundRaised),
          menuItemBackgroundHover = "menuItemBackgroundHover".attr(menuItemBackgroundHover),
          menuItemText = "menuItemText".attr(text),
          menuItemTextHover = "menuItemTextHover".attr(text),
          menuItemTextSelected = "menuItemTextSelected".attr(accentText),
          menuItemTextHeader = "menuItemTextHeader".attr(menuItemTextHeader),
          menuBorder = "menuBorder".attr(border),
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
          modalBackground = "modalBackground".attr(backgroundRaised),
          modalBorder = "modalBorder".attr(modalBorder),
          mobileHeaderBackground = "mobileHeaderBackground".attr(accent),
          mobileHeaderText = "mobileHeaderText".attr(accentContrast),
          mobileHeaderTextSubdued = "mobileHeaderTextSubdued".attr(mobileHeaderTextSubdued),
          mobileHeaderTextHover = "mobileHeaderTextHover".attr(mobileHeaderTextHover),
          mobilePageBackground = "mobilePageBackground".attr(mobilePageBackground),
          mobileNavBackground = "mobileNavBackground".attr(backgroundRaised),
          mobileNavItem = "mobileNavItem".attr(text),
          mobileNavItemSelected = "mobileNavItemSelected".attr(accentText),
          mobileAccountText = "mobileAccountText".attr(mobileAccountText),
          mobileTransactionSelected = "mobileTransactionSelected".attr(accentBorder),
          mobileViewTheme = "mobileViewTheme".attr(mobileViewTheme),
          mobileConfigServerViewTheme = "mobileConfigServerViewTheme".attr(accent),
          markdownNormal = "markdownNormal".attr(markdownNormal),
          markdownDark = "markdownDark".attr(accentBorder),
          markdownLight = "markdownLight".attr(accentSubtle),
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
          buttonPrimaryText = "buttonPrimaryText".attr(accentContrast),
          buttonPrimaryTextHover = "buttonPrimaryTextHover".attr(buttonPrimaryTextHover),
          buttonPrimaryBackground = "buttonPrimaryBackground".attr(accent),
          buttonPrimaryBackgroundHover = "buttonPrimaryBackgroundHover".attr(accentHover),
          buttonPrimaryBorder = "buttonPrimaryBorder".attr(buttonPrimaryBorder),
          buttonPrimaryShadow = "buttonPrimaryShadow".attr(buttonPrimaryShadow),
          buttonPrimaryDisabledText = "buttonPrimaryDisabledText".attr(buttonPrimaryDisabledText),
          buttonPrimaryDisabledBackground =
            "buttonPrimaryDisabledBackground".attr(buttonPrimaryDisabledBackground),
          buttonPrimaryDisabledBorder =
            "buttonPrimaryDisabledBorder".attr(buttonPrimaryDisabledBorder),
          buttonNormalText = "buttonNormalText".attr(text),
          buttonNormalTextHover = "buttonNormalTextHover".attr(text),
          buttonNormalBackground = "buttonNormalBackground".attr(backgroundRaised),
          buttonNormalBackgroundHover = "buttonNormalBackgroundHover".attr(backgroundHover),
          buttonNormalBorder = "buttonNormalBorder".attr(borderStrong),
          buttonNormalShadow = "buttonNormalShadow".attr(buttonNormalShadow),
          buttonNormalSelectedText = "buttonNormalSelectedText".attr(accentContrast),
          buttonNormalSelectedBackground = "buttonNormalSelectedBackground".attr(accent),
          buttonNormalDisabledText = "buttonNormalDisabledText".attr(buttonNormalDisabledText),
          buttonNormalDisabledBackground = "buttonNormalDisabledBackground".attr(backgroundRaised),
          buttonNormalDisabledBorder =
            "buttonNormalDisabledBorder".attr(buttonNormalDisabledBorder),
          calendarText = "calendarText".attr(calendarText),
          calendarBackground = "calendarBackground".attr(calendarBackground),
          calendarItemText = "calendarItemText".attr(calendarItemText),
          calendarItemBackground = "calendarItemBackground".attr(calendarItemBackground),
          calendarSelectedBackground = "calendarSelectedBackground".attr(accent),
          calendarSelectedText = "calendarSelectedText".attr(accentContrast),
          calendarCellBackground = "calendarCellBackground".attr(calendarCellBackground),
          datePickerRangeBackground = "datePickerRangeBackground".attr(accentSubtle),
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
          noticeText = "noticeText".attr(noticeText),
          noticeTextLight = "noticeTextLight".attr(noticeTextLight),
          noticeTextDark = "noticeTextDark".attr(noticeTextDark),
          noticeTextMenu = "noticeTextMenu".attr(noticeTextMenu),
          noticeBorder = "noticeBorder".attr(noticeBorder),
          warningBackground = "warningBackground".attr(warningBackground),
          warningText = "warningText".attr(warningText),
          warningTextDark = "warningTextDark".attr(warningTextDark),
          warningBorder = "warningBorder".attr(warningBorder),
          errorBackground = "errorBackground".attr(errorBackground),
          errorText = "errorText".attr(errorText),
          errorTextDark = "errorTextDark".attr(errorTextDark),
          errorTextDarker = "errorTextDarker".attr(errorTextDarker),
          errorTextMenu = "errorTextMenu".attr(errorTextMenu),
          errorBorder = "errorBorder".attr(errorBorder),
          upcomingBackground = "upcomingBackground".attr(accentSubtle),
          upcomingText = "upcomingText".attr(accentSubtleText),
        ),
      form =
        FormColors(
          formLabelText = "formLabelText".attr(accentText),
          formLabelBackground = "formLabelBackground".attr(formLabelBackground),
          formInputBackground = "formInputBackground".attr(background),
          formInputBackgroundSelected =
            "formInputBackgroundSelected".attr(formInputBackgroundSelected),
          formInputBackgroundSelection = "formInputBackgroundSelection".attr(accent),
          formInputBorder = "formInputBorder".attr(border),
          formInputTextReadOnlySelection =
            "formInputTextReadOnlySelection".attr(formInputTextReadOnlySelection),
          formInputBorderSelected = "formInputBorderSelected".attr(accentBorder),
          formInputText = "formInputText".attr(text),
          formInputTextPlaceholder = "formInputTextPlaceholder".attr(textTertiary),
          formInputTextPlaceholderSelected =
            "formInputTextPlaceholderSelected".attr(formInputTextPlaceholderSelected),
          formInputShadowSelected = "formInputShadowSelected".attr(accentBorder),
          formInputTextHighlight = "formInputTextHighlight".attr(accentText),
          checkboxText = "checkboxText".attr(accentContrast),
          checkboxBackgroundSelected = "checkboxBackgroundSelected".attr(accent),
          checkboxBorderSelected = "checkboxBorderSelected".attr(accent),
          checkboxShadowSelected = "checkboxShadowSelected".attr(accentBorder),
          checkboxToggleBackground = "checkboxToggleBackground".attr(checkboxToggleBackground),
          checkboxToggleBackgroundSelected = "checkboxToggleBackgroundSelected".attr(accent),
          checkboxToggleDisabled = "checkboxToggleDisabled".attr(checkboxToggleDisabled),
        ),
      misc =
        MiscColors(
          pillBackground = "pillBackground".attr(pillBackground),
          pillBackgroundLight = "pillBackgroundLight".attr(pillBackgroundLight),
          pillText = "pillText".attr(text),
          pillTextHighlighted = "pillTextHighlighted".attr(pillTextHighlighted),
          pillBorder = "pillBorder".attr(pillBorder),
          pillBorderDark = "pillBorderDark".attr(borderStrong),
          pillBackgroundSelected = "pillBackgroundSelected".attr(accentSubtle),
          pillTextSelected = "pillTextSelected".attr(accentSubtleText),
          pillBorderSelected = "pillBorderSelected".attr(accentBorder),
          reportsRed = "reportsRed".attr(reportsRed),
          reportsBlue = "reportsBlue".attr(reportsBlue),
          reportsGreen = "reportsGreen".attr(reportsGreen),
          reportsGray = "reportsGray".attr(reportsGray),
          reportsLabel = "reportsLabel".attr(text),
          reportsInnerLabel = "reportsInnerLabel".attr(reportsInnerLabel),
          reportsNumberPositive = "reportsNumberPositive".attr(reportsNumberPositive),
          reportsNumberNegative = "reportsNumberNegative".attr(reportsNumberNegative),
          reportsNumberNeutral = "reportsNumberNeutral".attr(reportsNumberNeutral),
          reportsChartFill = "reportsChartFill".attr(reportsChartFill),
          noteTagBackground = "noteTagBackground".attr(accentSubtle),
          noteTagBackgroundHover = "noteTagBackgroundHover".attr(noteTagBackgroundHover),
          noteTagText = "noteTagText".attr(accentSubtleText),
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
          floatingActionBarBackground = "floatingActionBarBackground".attr(accent),
          floatingActionBarBorder = "floatingActionBarBorder".attr(floatingActionBarBorder),
          floatingActionBarText = "floatingActionBarText".attr(accentContrast),
          tooltipText = "tooltipText".attr(text),
          tooltipBackground = "tooltipBackground".attr(backgroundRaised),
          tooltipBorder = "tooltipBorder".attr(border),
          overlayBackground = "overlayBackground".attr(overlayBackground),
        ),
      settings =
        SettingsColors(
          settingsCardBackground = "settingsCardBackground".attr(settingsCardBackground),
          settingsCardBorder = "settingsCardBorder".attr(settingsCardBorder),
          settingsCardText = "settingsCardText".attr(settingsCardText),
          settingsCardTextSubdued = "settingsCardTextSubdued".attr(textSecondary),
          settingsNavItemAccentSelected = "settingsNavItemAccentSelected".attr(accentSubtleText),
          settingsNavItemBackgroundHover =
            "settingsNavItemBackgroundHover".attr(settingsNavItemBackgroundHover),
          settingsNavItemBackgroundSelected =
            "settingsNavItemBackgroundSelected".attr(accentSubtle),
          settingsNavItemText = "settingsNavItemText".attr(settingsNavItemText),
          settingsNavItemTextSelected =
            "settingsNavItemTextSelected".attr(settingsNavItemTextSelected),
        ),
      accentColors =
        AccentColors(
          accent = accent,
          accentContrast = accentContrast,
          accentHover = accentHover,
          accentText = accentText,
          accentBorder = accentBorder,
          accentSubtle = accentSubtle,
          accentSubtleText = accentSubtleText,
        ),
      baseColors =
        BaseColors(
          backgroundPage = backgroundPage,
          background = background,
          backgroundRaised = backgroundRaised,
          backgroundHover = backgroundHover,
          backgroundSelected = backgroundSelected,
          border = border,
          borderStrong = borderStrong,
          text = text,
          textSecondary = textSecondary,
          textTertiary = textTertiary,
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
}

private const val TAG = "parseTheme"

private const val COLOR_PREFIX = "color-"

private val CommentRegex = """/\*[\s\S]*?\*/""".toRegex()

// Splitting on braces too means declarations on the same line as their selector are kept
private val DeclarationSeparator = "[;{}]".toRegex()

private val PropertyRegex = """--([\w-]+)\s*:([\s\S]*)""".toRegex()

private val VarRegex = """var\(\s*--([\w-]+)\s*(?:,\s*(.+?)\s*)?\)""".toRegex()

// Themes can declare their own properties alongside the --color- ones, and reference them by var()
private fun parsePropertyPair(declaration: String): Pair<String, String>? {
  val match = PropertyRegex.matchEntire(declaration)
  if (match == null) {
    log.w(TAG) { "Skipping declaration '$declaration', it doesn't match regex $PropertyRegex" }
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
  val match = VarRegex.matchEntire(value) ?: return value.parseColorOrNull()
  val (_, name, fallback) = match.groupValues
  val target = if (name in seen) null else get(name)
  return target?.let { resolveColor(it, seen + name) }
    ?: fallback.takeIf { it.isNotEmpty() }?.let { resolveColor(it, seen) }
}
