@file:UseSerializers(ColorLongSerializer::class)

package aktual.core.theme

import aktual.core.ui.Theme
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * Split up into separate sub-classes to avoid JVM runtime limitation:
 *
 * java.lang.ClassFormatError: Too many arguments in method signature in class file
 * aktual/core/theme/CustomTheme
 */
@Serializable
internal data class CustomTheme(
  @SerialName("page") val page: PageColors,
  @SerialName("table") val table: TableColors,
  @SerialName("navigation") val navigation: NavigationColors,
  @SerialName("modalMobile") val modalMobile: ModalMobileColors,
  @SerialName("button") val button: ButtonColors,
  @SerialName("status") val status: StatusColors,
  @SerialName("form") val form: FormColors,
  @SerialName("misc") val misc: MiscColors,
) : Theme {

  // Page
  override val pageBackground: Color
    get() = page.pageBackground

  override val pageBackgroundModalActive: Color
    get() = page.pageBackgroundModalActive

  override val pageBackgroundTopLeft: Color
    get() = page.pageBackgroundTopLeft

  override val pageBackgroundBottomRight: Color
    get() = page.pageBackgroundBottomRight

  override val pageBackgroundLineTop: Color
    get() = page.pageBackgroundLineTop

  override val pageBackgroundLineMid: Color
    get() = page.pageBackgroundLineMid

  override val pageBackgroundLineBottom: Color
    get() = page.pageBackgroundLineBottom

  override val pageText: Color
    get() = page.pageText

  override val pageTextLight: Color
    get() = page.pageTextLight

  override val pageTextSubdued: Color
    get() = page.pageTextSubdued

  override val pageTextDark: Color
    get() = page.pageTextDark

  override val pageTextPositive: Color
    get() = page.pageTextPositive

  override val pageTextLink: Color
    get() = page.pageTextLink

  override val pageTextLinkLight: Color
    get() = page.pageTextLinkLight

  override val numberPositive: Color
    get() = page.numberPositive

  override val numberNegative: Color
    get() = page.numberNegative

  override val numberNeutral: Color
    get() = page.numberNeutral

  override val cardBackground: Color
    get() = page.cardBackground

  override val cardBorder: Color
    get() = page.cardBorder

  override val cardShadow: Color
    get() = page.cardShadow

  // Table
  override val tableBackground: Color
    get() = table.tableBackground

  override val tableRowBackgroundHover: Color
    get() = table.tableRowBackgroundHover

  override val tableText: Color
    get() = table.tableText

  override val tableTextLight: Color
    get() = table.tableTextLight

  override val tableTextSubdued: Color
    get() = table.tableTextSubdued

  override val tableTextSelected: Color
    get() = table.tableTextSelected

  override val tableTextHover: Color
    get() = table.tableTextHover

  override val tableTextInactive: Color
    get() = table.tableTextInactive

  override val tableHeaderText: Color
    get() = table.tableHeaderText

  override val tableHeaderBackground: Color
    get() = table.tableHeaderBackground

  override val tableBorder: Color
    get() = table.tableBorder

  override val tableBorderSelected: Color
    get() = table.tableBorderSelected

  override val tableBorderHover: Color
    get() = table.tableBorderHover

  override val tableBorderSeparator: Color
    get() = table.tableBorderSeparator

  override val tableRowBackgroundHighlight: Color
    get() = table.tableRowBackgroundHighlight

  override val tableRowBackgroundHighlightText: Color
    get() = table.tableRowBackgroundHighlightText

  override val tableRowHeaderBackground: Color
    get() = table.tableRowHeaderBackground

  override val tableRowHeaderText: Color
    get() = table.tableRowHeaderText

  // Sidebar + Menu
  override val sidebarBackground: Color
    get() = navigation.sidebarBackground

  override val sidebarItemBackgroundPending: Color
    get() = navigation.sidebarItemBackgroundPending

  override val sidebarItemBackgroundPositive: Color
    get() = navigation.sidebarItemBackgroundPositive

  override val sidebarItemBackgroundFailed: Color
    get() = navigation.sidebarItemBackgroundFailed

  override val sidebarItemAccentSelected: Color
    get() = navigation.sidebarItemAccentSelected

  override val sidebarItemBackgroundHover: Color
    get() = navigation.sidebarItemBackgroundHover

  override val sidebarItemText: Color
    get() = navigation.sidebarItemText

  override val sidebarItemTextSelected: Color
    get() = navigation.sidebarItemTextSelected

  override val sidebarBudgetName: Color
    get() = navigation.sidebarBudgetName

  override val menuBackground: Color
    get() = navigation.menuBackground

  override val menuItemBackground: Color
    get() = navigation.menuItemBackground

  override val menuItemBackgroundHover: Color
    get() = navigation.menuItemBackgroundHover

  override val menuItemText: Color
    get() = navigation.menuItemText

  override val menuItemTextHover: Color
    get() = navigation.menuItemTextHover

  override val menuItemTextSelected: Color
    get() = navigation.menuItemTextSelected

  override val menuItemTextHeader: Color
    get() = navigation.menuItemTextHeader

  override val menuBorder: Color
    get() = navigation.menuBorder

  override val menuBorderHover: Color
    get() = navigation.menuBorderHover

  override val menuKeybindingText: Color
    get() = navigation.menuKeybindingText

  override val menuAutoCompleteBackground: Color
    get() = navigation.menuAutoCompleteBackground

  override val menuAutoCompleteBackgroundHover: Color
    get() = navigation.menuAutoCompleteBackgroundHover

  override val menuAutoCompleteText: Color
    get() = navigation.menuAutoCompleteText

  override val menuAutoCompleteTextHover: Color
    get() = navigation.menuAutoCompleteTextHover

  override val menuAutoCompleteTextHeader: Color
    get() = navigation.menuAutoCompleteTextHeader

  override val menuAutoCompleteItemTextHover: Color
    get() = navigation.menuAutoCompleteItemTextHover

  override val menuAutoCompleteItemText: Color
    get() = navigation.menuAutoCompleteItemText

  // Modal + Mobile + Markdown
  override val modalBackground: Color
    get() = modalMobile.modalBackground

  override val modalBorder: Color
    get() = modalMobile.modalBorder

  override val mobileHeaderBackground: Color
    get() = modalMobile.mobileHeaderBackground

  override val mobileHeaderText: Color
    get() = modalMobile.mobileHeaderText

  override val mobileHeaderTextSubdued: Color
    get() = modalMobile.mobileHeaderTextSubdued

  override val mobileHeaderTextHover: Color
    get() = modalMobile.mobileHeaderTextHover

  override val mobilePageBackground: Color
    get() = modalMobile.mobilePageBackground

  override val mobileNavBackground: Color
    get() = modalMobile.mobileNavBackground

  override val mobileNavItem: Color
    get() = modalMobile.mobileNavItem

  override val mobileNavItemSelected: Color
    get() = modalMobile.mobileNavItemSelected

  override val mobileAccountShadow: Color
    get() = modalMobile.mobileAccountShadow

  override val mobileAccountText: Color
    get() = modalMobile.mobileAccountText

  override val mobileTransactionSelected: Color
    get() = modalMobile.mobileTransactionSelected

  override val mobileViewTheme: Color
    get() = modalMobile.mobileViewTheme

  override val mobileConfigServerViewTheme: Color
    get() = modalMobile.mobileConfigServerViewTheme

  override val markdownNormal: Color
    get() = modalMobile.markdownNormal

  override val markdownDark: Color
    get() = modalMobile.markdownDark

  override val markdownLight: Color
    get() = modalMobile.markdownLight

  // Buttons + Calendar
  override val buttonMenuText: Color
    get() = button.buttonMenuText

  override val buttonMenuTextHover: Color
    get() = button.buttonMenuTextHover

  override val buttonMenuBackground: Color
    get() = button.buttonMenuBackground

  override val buttonMenuBackgroundHover: Color
    get() = button.buttonMenuBackgroundHover

  override val buttonMenuBorder: Color
    get() = button.buttonMenuBorder

  override val buttonMenuSelectedText: Color
    get() = button.buttonMenuSelectedText

  override val buttonMenuSelectedTextHover: Color
    get() = button.buttonMenuSelectedTextHover

  override val buttonMenuSelectedBackground: Color
    get() = button.buttonMenuSelectedBackground

  override val buttonMenuSelectedBackgroundHover: Color
    get() = button.buttonMenuSelectedBackgroundHover

  override val buttonMenuSelectedBorder: Color
    get() = button.buttonMenuSelectedBorder

  override val buttonPrimaryText: Color
    get() = button.buttonPrimaryText

  override val buttonPrimaryTextHover: Color
    get() = button.buttonPrimaryTextHover

  override val buttonPrimaryBackground: Color
    get() = button.buttonPrimaryBackground

  override val buttonPrimaryBackgroundHover: Color
    get() = button.buttonPrimaryBackgroundHover

  override val buttonPrimaryBorder: Color
    get() = button.buttonPrimaryBorder

  override val buttonPrimaryShadow: Color
    get() = button.buttonPrimaryShadow

  override val buttonPrimaryDisabledText: Color
    get() = button.buttonPrimaryDisabledText

  override val buttonPrimaryDisabledBackground: Color
    get() = button.buttonPrimaryDisabledBackground

  override val buttonPrimaryDisabledBorder: Color
    get() = button.buttonPrimaryDisabledBorder

  override val buttonNormalText: Color
    get() = button.buttonNormalText

  override val buttonNormalTextHover: Color
    get() = button.buttonNormalTextHover

  override val buttonNormalBackground: Color
    get() = button.buttonNormalBackground

  override val buttonNormalBackgroundHover: Color
    get() = button.buttonNormalBackgroundHover

  override val buttonNormalBorder: Color
    get() = button.buttonNormalBorder

  override val buttonNormalShadow: Color
    get() = button.buttonNormalShadow

  override val buttonNormalSelectedText: Color
    get() = button.buttonNormalSelectedText

  override val buttonNormalSelectedBackground: Color
    get() = button.buttonNormalSelectedBackground

  override val buttonNormalDisabledText: Color
    get() = button.buttonNormalDisabledText

  override val buttonNormalDisabledBackground: Color
    get() = button.buttonNormalDisabledBackground

  override val buttonNormalDisabledBorder: Color
    get() = button.buttonNormalDisabledBorder

  override val calendarText: Color
    get() = button.calendarText

  override val calendarBackground: Color
    get() = button.calendarBackground

  override val calendarItemText: Color
    get() = button.calendarItemText

  override val calendarItemBackground: Color
    get() = button.calendarItemBackground

  override val calendarSelectedBackground: Color
    get() = button.calendarSelectedBackground

  override val calendarCellBackground: Color
    get() = button.calendarCellBackground

  override val buttonBareText: Color
    get() = button.buttonBareText

  override val buttonBareTextHover: Color
    get() = button.buttonBareTextHover

  override val buttonBareBackground: Color
    get() = button.buttonBareBackground

  override val buttonBareBackgroundHover: Color
    get() = button.buttonBareBackgroundHover

  override val buttonBareBackgroundActive: Color
    get() = button.buttonBareBackgroundActive

  override val buttonBareDisabledText: Color
    get() = button.buttonBareDisabledText

  override val buttonBareDisabledBackground: Color
    get() = button.buttonBareDisabledBackground

  // Status (notice, warning, error, upcoming, success)
  override val noticeBackground: Color
    get() = status.noticeBackground

  override val noticeBackgroundLight: Color
    get() = status.noticeBackgroundLight

  override val noticeBackgroundDark: Color
    get() = status.noticeBackgroundDark

  override val noticeText: Color
    get() = status.noticeText

  override val noticeTextLight: Color
    get() = status.noticeTextLight

  override val noticeTextDark: Color
    get() = status.noticeTextDark

  override val noticeTextMenu: Color
    get() = status.noticeTextMenu

  override val noticeBorder: Color
    get() = status.noticeBorder

  override val warningBackground: Color
    get() = status.warningBackground

  override val warningText: Color
    get() = status.warningText

  override val warningTextLight: Color
    get() = status.warningTextLight

  override val warningTextDark: Color
    get() = status.warningTextDark

  override val warningBorder: Color
    get() = status.warningBorder

  override val errorBackground: Color
    get() = status.errorBackground

  override val errorText: Color
    get() = status.errorText

  override val errorTextDark: Color
    get() = status.errorTextDark

  override val errorTextDarker: Color
    get() = status.errorTextDarker

  override val errorTextMenu: Color
    get() = status.errorTextMenu

  override val errorBorder: Color
    get() = status.errorBorder

  override val upcomingBackground: Color
    get() = status.upcomingBackground

  override val upcomingText: Color
    get() = status.upcomingText

  override val upcomingBorder: Color
    get() = status.upcomingBorder

  // Form + Checkbox
  override val formLabelText: Color
    get() = form.formLabelText

  override val formLabelBackground: Color
    get() = form.formLabelBackground

  override val formInputBackground: Color
    get() = form.formInputBackground

  override val formInputBackgroundSelected: Color
    get() = form.formInputBackgroundSelected

  override val formInputBackgroundSelection: Color
    get() = form.formInputBackgroundSelection

  override val formInputBorder: Color
    get() = form.formInputBorder

  override val formInputTextReadOnlySelection: Color
    get() = form.formInputTextReadOnlySelection

  override val formInputBorderSelected: Color
    get() = form.formInputBorderSelected

  override val formInputText: Color
    get() = form.formInputText

  override val formInputTextSelected: Color
    get() = form.formInputTextSelected

  override val formInputTextPlaceholder: Color
    get() = form.formInputTextPlaceholder

  override val formInputTextPlaceholderSelected: Color
    get() = form.formInputTextPlaceholderSelected

  override val formInputTextSelection: Color
    get() = form.formInputTextSelection

  override val formInputShadowSelected: Color
    get() = form.formInputShadowSelected

  override val formInputTextHighlight: Color
    get() = form.formInputTextHighlight

  override val checkboxText: Color
    get() = form.checkboxText

  override val checkboxBackgroundSelected: Color
    get() = form.checkboxBackgroundSelected

  override val checkboxBorderSelected: Color
    get() = form.checkboxBorderSelected

  override val checkboxShadowSelected: Color
    get() = form.checkboxShadowSelected

  override val checkboxToggleBackground: Color
    get() = form.checkboxToggleBackground

  override val checkboxToggleBackgroundSelected: Color
    get() = form.checkboxToggleBackgroundSelected

  override val checkboxToggleDisabled: Color
    get() = form.checkboxToggleDisabled

  // Misc (pill, reports, scrollbar, slider, dialog, budgetItem)
  override val pillBackground: Color
    get() = misc.pillBackground

  override val pillBackgroundLight: Color
    get() = misc.pillBackgroundLight

  override val pillText: Color
    get() = misc.pillText

  override val pillTextHighlighted: Color
    get() = misc.pillTextHighlighted

  override val pillBorder: Color
    get() = misc.pillBorder

  override val pillBorderDark: Color
    get() = misc.pillBorderDark

  override val pillBackgroundSelected: Color
    get() = misc.pillBackgroundSelected

  override val pillTextSelected: Color
    get() = misc.pillTextSelected

  override val pillBorderSelected: Color
    get() = misc.pillBorderSelected

  override val reportsRed: Color
    get() = misc.reportsRed

  override val reportsBlue: Color
    get() = misc.reportsBlue

  override val reportsGreen: Color
    get() = misc.reportsGreen

  override val reportsGray: Color
    get() = misc.reportsGray

  override val reportsLabel: Color
    get() = misc.reportsLabel

  override val reportsInnerLabel: Color
    get() = misc.reportsInnerLabel

  override val reportsNumberPositive: Color
    get() = misc.reportsNumberPositive

  override val reportsNumberNegative: Color
    get() = misc.reportsNumberNegative

  override val reportsNumberNeutral: Color
    get() = misc.reportsNumberNeutral

  override val reportsChartFill: Color
    get() = misc.reportsChartFill

  override val pillTextSubdued: Color
    get() = misc.pillTextSubdued

  override val noteTagBackground: Color
    get() = misc.noteTagBackground

  override val noteTagBackgroundHover: Color
    get() = misc.noteTagBackgroundHover

  override val noteTagDefault: Color
    get() = misc.noteTagDefault

  override val noteTagText: Color
    get() = misc.noteTagText

  override val budgetCurrentMonth: Color
    get() = misc.budgetCurrentMonth

  override val budgetOtherMonth: Color
    get() = misc.budgetOtherMonth

  override val budgetHeaderCurrentMonth: Color
    get() = misc.budgetHeaderCurrentMonth

  override val budgetHeaderOtherMonth: Color
    get() = misc.budgetHeaderOtherMonth

  override val budgetNumberZero: Color
    get() = misc.budgetNumberZero

  override val budgetNumberNegative: Color
    get() = misc.budgetNumberNegative

  override val budgetNumberNeutral: Color
    get() = misc.budgetNumberNeutral

  override val budgetNumberPositive: Color
    get() = misc.budgetNumberPositive

  override val templateNumberFunded: Color
    get() = misc.templateNumberFunded

  override val templateNumberUnderFunded: Color
    get() = misc.templateNumberUnderFunded

  override val toBudgetPositive: Color
    get() = misc.toBudgetPositive

  override val toBudgetZero: Color
    get() = misc.toBudgetZero

  override val toBudgetNegative: Color
    get() = misc.toBudgetNegative

  override val floatingActionBarBackground: Color
    get() = misc.floatingActionBarBackground

  override val floatingActionBarBorder: Color
    get() = misc.floatingActionBarBorder

  override val floatingActionBarText: Color
    get() = misc.floatingActionBarText

  override val tooltipText: Color
    get() = misc.tooltipText

  override val tooltipBackground: Color
    get() = misc.tooltipBackground

  override val tooltipBorder: Color
    get() = misc.tooltipBorder

  override val overlayBackground: Color
    get() = misc.overlayBackground
}

@Serializable
internal data class PageColors(
  @SerialName("pageBackground") val pageBackground: Color,
  @SerialName("pageBackgroundModalActive") val pageBackgroundModalActive: Color,
  @SerialName("pageBackgroundTopLeft") val pageBackgroundTopLeft: Color,
  @SerialName("pageBackgroundBottomRight") val pageBackgroundBottomRight: Color,
  @SerialName("pageBackgroundLineTop") val pageBackgroundLineTop: Color,
  @SerialName("pageBackgroundLineMid") val pageBackgroundLineMid: Color,
  @SerialName("pageBackgroundLineBottom") val pageBackgroundLineBottom: Color,
  @SerialName("pageText") val pageText: Color,
  @SerialName("pageTextLight") val pageTextLight: Color,
  @SerialName("pageTextSubdued") val pageTextSubdued: Color,
  @SerialName("pageTextDark") val pageTextDark: Color,
  @SerialName("pageTextPositive") val pageTextPositive: Color,
  @SerialName("pageTextLink") val pageTextLink: Color,
  @SerialName("pageTextLinkLight") val pageTextLinkLight: Color,
  @SerialName("numberPositive") val numberPositive: Color,
  @SerialName("numberNegative") val numberNegative: Color,
  @SerialName("numberNeutral") val numberNeutral: Color,
  @SerialName("cardBackground") val cardBackground: Color,
  @SerialName("cardBorder") val cardBorder: Color,
  @SerialName("cardShadow") val cardShadow: Color,
)

@Serializable
internal data class TableColors(
  @SerialName("tableBackground") val tableBackground: Color,
  @SerialName("tableRowBackgroundHover") val tableRowBackgroundHover: Color,
  @SerialName("tableText") val tableText: Color,
  @SerialName("tableTextLight") val tableTextLight: Color,
  @SerialName("tableTextSubdued") val tableTextSubdued: Color,
  @SerialName("tableTextSelected") val tableTextSelected: Color,
  @SerialName("tableTextHover") val tableTextHover: Color,
  @SerialName("tableTextInactive") val tableTextInactive: Color,
  @SerialName("tableHeaderText") val tableHeaderText: Color,
  @SerialName("tableHeaderBackground") val tableHeaderBackground: Color,
  @SerialName("tableBorder") val tableBorder: Color,
  @SerialName("tableBorderSelected") val tableBorderSelected: Color,
  @SerialName("tableBorderHover") val tableBorderHover: Color,
  @SerialName("tableBorderSeparator") val tableBorderSeparator: Color,
  @SerialName("tableRowBackgroundHighlight") val tableRowBackgroundHighlight: Color,
  @SerialName("tableRowBackgroundHighlightText") val tableRowBackgroundHighlightText: Color,
  @SerialName("tableRowHeaderBackground") val tableRowHeaderBackground: Color,
  @SerialName("tableRowHeaderText") val tableRowHeaderText: Color,
)

@Serializable
internal data class NavigationColors(
  @SerialName("sidebarBackground") val sidebarBackground: Color,
  @SerialName("sidebarItemBackgroundPending") val sidebarItemBackgroundPending: Color,
  @SerialName("sidebarItemBackgroundPositive") val sidebarItemBackgroundPositive: Color,
  @SerialName("sidebarItemBackgroundFailed") val sidebarItemBackgroundFailed: Color,
  @SerialName("sidebarItemAccentSelected") val sidebarItemAccentSelected: Color,
  @SerialName("sidebarItemBackgroundHover") val sidebarItemBackgroundHover: Color,
  @SerialName("sidebarItemText") val sidebarItemText: Color,
  @SerialName("sidebarItemTextSelected") val sidebarItemTextSelected: Color,
  @SerialName("sidebarBudgetName") val sidebarBudgetName: Color,
  @SerialName("menuBackground") val menuBackground: Color,
  @SerialName("menuItemBackground") val menuItemBackground: Color,
  @SerialName("menuItemBackgroundHover") val menuItemBackgroundHover: Color,
  @SerialName("menuItemText") val menuItemText: Color,
  @SerialName("menuItemTextHover") val menuItemTextHover: Color,
  @SerialName("menuItemTextSelected") val menuItemTextSelected: Color,
  @SerialName("menuItemTextHeader") val menuItemTextHeader: Color,
  @SerialName("menuBorder") val menuBorder: Color,
  @SerialName("menuBorderHover") val menuBorderHover: Color,
  @SerialName("menuKeybindingText") val menuKeybindingText: Color,
  @SerialName("menuAutoCompleteBackground") val menuAutoCompleteBackground: Color,
  @SerialName("menuAutoCompleteBackgroundHover") val menuAutoCompleteBackgroundHover: Color,
  @SerialName("menuAutoCompleteText") val menuAutoCompleteText: Color,
  @SerialName("menuAutoCompleteTextHover") val menuAutoCompleteTextHover: Color,
  @SerialName("menuAutoCompleteTextHeader") val menuAutoCompleteTextHeader: Color,
  @SerialName("menuAutoCompleteItemTextHover") val menuAutoCompleteItemTextHover: Color,
  @SerialName("menuAutoCompleteItemText") val menuAutoCompleteItemText: Color,
)

@Serializable
internal data class ModalMobileColors(
  @SerialName("modalBackground") val modalBackground: Color,
  @SerialName("modalBorder") val modalBorder: Color,
  @SerialName("mobileHeaderBackground") val mobileHeaderBackground: Color,
  @SerialName("mobileHeaderText") val mobileHeaderText: Color,
  @SerialName("mobileHeaderTextSubdued") val mobileHeaderTextSubdued: Color,
  @SerialName("mobileHeaderTextHover") val mobileHeaderTextHover: Color,
  @SerialName("mobilePageBackground") val mobilePageBackground: Color,
  @SerialName("mobileNavBackground") val mobileNavBackground: Color,
  @SerialName("mobileNavItem") val mobileNavItem: Color,
  @SerialName("mobileNavItemSelected") val mobileNavItemSelected: Color,
  @SerialName("mobileAccountShadow") val mobileAccountShadow: Color,
  @SerialName("mobileAccountText") val mobileAccountText: Color,
  @SerialName("mobileTransactionSelected") val mobileTransactionSelected: Color,
  @SerialName("mobileViewTheme") val mobileViewTheme: Color,
  @SerialName("mobileConfigServerViewTheme") val mobileConfigServerViewTheme: Color,
  @SerialName("markdownNormal") val markdownNormal: Color,
  @SerialName("markdownDark") val markdownDark: Color,
  @SerialName("markdownLight") val markdownLight: Color,
)

@Serializable
internal data class ButtonColors(
  @SerialName("buttonMenuText") val buttonMenuText: Color,
  @SerialName("buttonMenuTextHover") val buttonMenuTextHover: Color,
  @SerialName("buttonMenuBackground") val buttonMenuBackground: Color,
  @SerialName("buttonMenuBackgroundHover") val buttonMenuBackgroundHover: Color,
  @SerialName("buttonMenuBorder") val buttonMenuBorder: Color,
  @SerialName("buttonMenuSelectedText") val buttonMenuSelectedText: Color,
  @SerialName("buttonMenuSelectedTextHover") val buttonMenuSelectedTextHover: Color,
  @SerialName("buttonMenuSelectedBackground") val buttonMenuSelectedBackground: Color,
  @SerialName("buttonMenuSelectedBackgroundHover") val buttonMenuSelectedBackgroundHover: Color,
  @SerialName("buttonMenuSelectedBorder") val buttonMenuSelectedBorder: Color,
  @SerialName("buttonPrimaryText") val buttonPrimaryText: Color,
  @SerialName("buttonPrimaryTextHover") val buttonPrimaryTextHover: Color,
  @SerialName("buttonPrimaryBackground") val buttonPrimaryBackground: Color,
  @SerialName("buttonPrimaryBackgroundHover") val buttonPrimaryBackgroundHover: Color,
  @SerialName("buttonPrimaryBorder") val buttonPrimaryBorder: Color,
  @SerialName("buttonPrimaryShadow") val buttonPrimaryShadow: Color,
  @SerialName("buttonPrimaryDisabledText") val buttonPrimaryDisabledText: Color,
  @SerialName("buttonPrimaryDisabledBackground") val buttonPrimaryDisabledBackground: Color,
  @SerialName("buttonPrimaryDisabledBorder") val buttonPrimaryDisabledBorder: Color,
  @SerialName("buttonNormalText") val buttonNormalText: Color,
  @SerialName("buttonNormalTextHover") val buttonNormalTextHover: Color,
  @SerialName("buttonNormalBackground") val buttonNormalBackground: Color,
  @SerialName("buttonNormalBackgroundHover") val buttonNormalBackgroundHover: Color,
  @SerialName("buttonNormalBorder") val buttonNormalBorder: Color,
  @SerialName("buttonNormalShadow") val buttonNormalShadow: Color,
  @SerialName("buttonNormalSelectedText") val buttonNormalSelectedText: Color,
  @SerialName("buttonNormalSelectedBackground") val buttonNormalSelectedBackground: Color,
  @SerialName("buttonNormalDisabledText") val buttonNormalDisabledText: Color,
  @SerialName("buttonNormalDisabledBackground") val buttonNormalDisabledBackground: Color,
  @SerialName("buttonNormalDisabledBorder") val buttonNormalDisabledBorder: Color,
  @SerialName("calendarText") val calendarText: Color,
  @SerialName("calendarBackground") val calendarBackground: Color,
  @SerialName("calendarItemText") val calendarItemText: Color,
  @SerialName("calendarItemBackground") val calendarItemBackground: Color,
  @SerialName("calendarSelectedBackground") val calendarSelectedBackground: Color,
  @SerialName("calendarCellBackground") val calendarCellBackground: Color,
  @SerialName("buttonBareText") val buttonBareText: Color,
  @SerialName("buttonBareTextHover") val buttonBareTextHover: Color,
  @SerialName("buttonBareBackground") val buttonBareBackground: Color,
  @SerialName("buttonBareBackgroundHover") val buttonBareBackgroundHover: Color,
  @SerialName("buttonBareBackgroundActive") val buttonBareBackgroundActive: Color,
  @SerialName("buttonBareDisabledText") val buttonBareDisabledText: Color,
  @SerialName("buttonBareDisabledBackground") val buttonBareDisabledBackground: Color,
)

@Serializable
internal data class StatusColors(
  @SerialName("noticeBackground") val noticeBackground: Color,
  @SerialName("noticeBackgroundLight") val noticeBackgroundLight: Color,
  @SerialName("noticeBackgroundDark") val noticeBackgroundDark: Color,
  @SerialName("noticeText") val noticeText: Color,
  @SerialName("noticeTextLight") val noticeTextLight: Color,
  @SerialName("noticeTextDark") val noticeTextDark: Color,
  @SerialName("noticeTextMenu") val noticeTextMenu: Color,
  @SerialName("noticeBorder") val noticeBorder: Color,
  @SerialName("warningBackground") val warningBackground: Color,
  @SerialName("warningText") val warningText: Color,
  @SerialName("warningTextLight") val warningTextLight: Color,
  @SerialName("warningTextDark") val warningTextDark: Color,
  @SerialName("warningBorder") val warningBorder: Color,
  @SerialName("errorBackground") val errorBackground: Color,
  @SerialName("errorText") val errorText: Color,
  @SerialName("errorTextDark") val errorTextDark: Color,
  @SerialName("errorTextDarker") val errorTextDarker: Color,
  @SerialName("errorTextMenu") val errorTextMenu: Color,
  @SerialName("errorBorder") val errorBorder: Color,
  @SerialName("upcomingBackground") val upcomingBackground: Color,
  @SerialName("upcomingText") val upcomingText: Color,
  @SerialName("upcomingBorder") val upcomingBorder: Color,
)

@Serializable
internal data class FormColors(
  @SerialName("formLabelText") val formLabelText: Color,
  @SerialName("formLabelBackground") val formLabelBackground: Color,
  @SerialName("formInputBackground") val formInputBackground: Color,
  @SerialName("formInputBackgroundSelected") val formInputBackgroundSelected: Color,
  @SerialName("formInputBackgroundSelection") val formInputBackgroundSelection: Color,
  @SerialName("formInputBorder") val formInputBorder: Color,
  @SerialName("formInputTextReadOnlySelection") val formInputTextReadOnlySelection: Color,
  @SerialName("formInputBorderSelected") val formInputBorderSelected: Color,
  @SerialName("formInputText") val formInputText: Color,
  @SerialName("formInputTextSelected") val formInputTextSelected: Color,
  @SerialName("formInputTextPlaceholder") val formInputTextPlaceholder: Color,
  @SerialName("formInputTextPlaceholderSelected") val formInputTextPlaceholderSelected: Color,
  @SerialName("formInputTextSelection") val formInputTextSelection: Color,
  @SerialName("formInputShadowSelected") val formInputShadowSelected: Color,
  @SerialName("formInputTextHighlight") val formInputTextHighlight: Color,
  @SerialName("checkboxText") val checkboxText: Color,
  @SerialName("checkboxBackgroundSelected") val checkboxBackgroundSelected: Color,
  @SerialName("checkboxBorderSelected") val checkboxBorderSelected: Color,
  @SerialName("checkboxShadowSelected") val checkboxShadowSelected: Color,
  @SerialName("checkboxToggleBackground") val checkboxToggleBackground: Color,
  @SerialName("checkboxToggleBackgroundSelected") val checkboxToggleBackgroundSelected: Color,
  @SerialName("checkboxToggleDisabled") val checkboxToggleDisabled: Color,
)

@Serializable
internal data class MiscColors(
  @SerialName("pillBackground") val pillBackground: Color,
  @SerialName("pillBackgroundLight") val pillBackgroundLight: Color,
  @SerialName("pillText") val pillText: Color,
  @SerialName("pillTextHighlighted") val pillTextHighlighted: Color,
  @SerialName("pillBorder") val pillBorder: Color,
  @SerialName("pillBorderDark") val pillBorderDark: Color,
  @SerialName("pillBackgroundSelected") val pillBackgroundSelected: Color,
  @SerialName("pillTextSelected") val pillTextSelected: Color,
  @SerialName("pillBorderSelected") val pillBorderSelected: Color,
  @SerialName("pillTextSubdued") val pillTextSubdued: Color,
  @SerialName("reportsRed") val reportsRed: Color,
  @SerialName("reportsBlue") val reportsBlue: Color,
  @SerialName("reportsGreen") val reportsGreen: Color,
  @SerialName("reportsGray") val reportsGray: Color,
  @SerialName("reportsLabel") val reportsLabel: Color,
  @SerialName("reportsInnerLabel") val reportsInnerLabel: Color,
  @SerialName("reportsNumberPositive") val reportsNumberPositive: Color,
  @SerialName("reportsNumberNegative") val reportsNumberNegative: Color,
  @SerialName("reportsNumberNeutral") val reportsNumberNeutral: Color,
  @SerialName("reportsChartFill") val reportsChartFill: Color,
  @SerialName("noteTagBackground") val noteTagBackground: Color,
  @SerialName("noteTagBackgroundHover") val noteTagBackgroundHover: Color,
  @SerialName("noteTagDefault") val noteTagDefault: Color,
  @SerialName("noteTagText") val noteTagText: Color,
  @SerialName("budgetCurrentMonth") val budgetCurrentMonth: Color,
  @SerialName("budgetOtherMonth") val budgetOtherMonth: Color,
  @SerialName("budgetHeaderCurrentMonth") val budgetHeaderCurrentMonth: Color,
  @SerialName("budgetHeaderOtherMonth") val budgetHeaderOtherMonth: Color,
  @SerialName("budgetNumberZero") val budgetNumberZero: Color,
  @SerialName("budgetNumberNegative") val budgetNumberNegative: Color,
  @SerialName("budgetNumberNeutral") val budgetNumberNeutral: Color,
  @SerialName("budgetNumberPositive") val budgetNumberPositive: Color,
  @SerialName("templateNumberFunded") val templateNumberFunded: Color,
  @SerialName("templateNumberUnderFunded") val templateNumberUnderFunded: Color,
  @SerialName("toBudgetPositive") val toBudgetPositive: Color,
  @SerialName("toBudgetZero") val toBudgetZero: Color,
  @SerialName("toBudgetNegative") val toBudgetNegative: Color,
  @SerialName("floatingActionBarBackground") val floatingActionBarBackground: Color,
  @SerialName("floatingActionBarBorder") val floatingActionBarBorder: Color,
  @SerialName("floatingActionBarText") val floatingActionBarText: Color,
  @SerialName("tooltipText") val tooltipText: Color,
  @SerialName("tooltipBackground") val tooltipBackground: Color,
  @SerialName("tooltipBorder") val tooltipBorder: Color,
  @SerialName("overlayBackground") val overlayBackground: Color,
)
