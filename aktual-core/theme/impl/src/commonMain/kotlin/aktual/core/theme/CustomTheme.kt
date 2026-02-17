@file:UseSerializers(ColorLongSerializer::class)

package aktual.core.theme

import aktual.core.ui.Theme
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * Split up into separate sub-classes to avoid JVM runtime limitation:
 *
 *   java.lang.ClassFormatError: Too many arguments in method signature in class
 *   file aktual/core/theme/CustomTheme
 */
@Serializable
internal data class CustomTheme(
  private val page: PageColors,
  private val table: TableColors,
  private val navigation: NavigationColors,
  private val modalMobile: ModalMobileColors,
  private val button: ButtonColors,
  private val status: StatusColors,
  private val form: FormColors,
  private val misc: MiscColors,
) : Theme {

  // Page
  override val pageBackground: Color get() = page.pageBackground
  override val pageBackgroundModalActive: Color get() = page.pageBackgroundModalActive
  override val pageBackgroundTopLeft: Color get() = page.pageBackgroundTopLeft
  override val pageBackgroundBottomRight: Color get() = page.pageBackgroundBottomRight
  override val pageBackgroundLineTop: Color get() = page.pageBackgroundLineTop
  override val pageBackgroundLineMid: Color get() = page.pageBackgroundLineMid
  override val pageBackgroundLineBottom: Color get() = page.pageBackgroundLineBottom
  override val pageText: Color get() = page.pageText
  override val pageTextLight: Color get() = page.pageTextLight
  override val pageTextSubdued: Color get() = page.pageTextSubdued
  override val pageTextDark: Color get() = page.pageTextDark
  override val pageTextPositive: Color get() = page.pageTextPositive
  override val pageTextLink: Color get() = page.pageTextLink
  override val pageTextLinkLight: Color get() = page.pageTextLinkLight
  override val numberPositive: Color get() = page.numberPositive
  override val numberNegative: Color get() = page.numberNegative
  override val numberNeutral: Color get() = page.numberNeutral
  override val cardBackground: Color get() = page.cardBackground
  override val cardBorder: Color get() = page.cardBorder
  override val cardShadow: Color get() = page.cardShadow

  // Table
  override val tableBackground: Color get() = table.tableBackground
  override val tableRowBackgroundHover: Color get() = table.tableRowBackgroundHover
  override val tableText: Color get() = table.tableText
  override val tableTextLight: Color get() = table.tableTextLight
  override val tableTextSubdued: Color get() = table.tableTextSubdued
  override val tableTextSelected: Color get() = table.tableTextSelected
  override val tableTextHover: Color get() = table.tableTextHover
  override val tableTextInactive: Color get() = table.tableTextInactive
  override val tableHeaderText: Color get() = table.tableHeaderText
  override val tableHeaderBackground: Color get() = table.tableHeaderBackground
  override val tableBorder: Color get() = table.tableBorder
  override val tableBorderSelected: Color get() = table.tableBorderSelected
  override val tableBorderHover: Color get() = table.tableBorderHover
  override val tableBorderSeparator: Color get() = table.tableBorderSeparator
  override val tableRowBackgroundHighlight: Color get() = table.tableRowBackgroundHighlight
  override val tableRowBackgroundHighlightText: Color get() = table.tableRowBackgroundHighlightText
  override val tableRowHeaderBackground: Color get() = table.tableRowHeaderBackground
  override val tableRowHeaderText: Color get() = table.tableRowHeaderText

  // Sidebar + Menu
  override val sidebarBackground: Color get() = navigation.sidebarBackground
  override val sidebarItemBackgroundPositive: Color get() = navigation.sidebarItemBackgroundPositive
  override val sidebarItemBackgroundFailed: Color get() = navigation.sidebarItemBackgroundFailed
  override val sidebarItemAccentSelected: Color get() = navigation.sidebarItemAccentSelected
  override val sidebarItemBackgroundHover: Color get() = navigation.sidebarItemBackgroundHover
  override val sidebarItemText: Color get() = navigation.sidebarItemText
  override val sidebarItemTextSelected: Color get() = navigation.sidebarItemTextSelected
  override val menuBackground: Color get() = navigation.menuBackground
  override val menuItemBackground: Color get() = navigation.menuItemBackground
  override val menuItemBackgroundHover: Color get() = navigation.menuItemBackgroundHover
  override val menuItemText: Color get() = navigation.menuItemText
  override val menuItemTextHover: Color get() = navigation.menuItemTextHover
  override val menuItemTextSelected: Color get() = navigation.menuItemTextSelected
  override val menuItemTextHeader: Color get() = navigation.menuItemTextHeader
  override val menuBorder: Color get() = navigation.menuBorder
  override val menuBorderHover: Color get() = navigation.menuBorderHover
  override val menuKeybindingText: Color get() = navigation.menuKeybindingText
  override val menuAutoCompleteBackground: Color get() = navigation.menuAutoCompleteBackground
  override val menuAutoCompleteBackgroundHover: Color get() = navigation.menuAutoCompleteBackgroundHover
  override val menuAutoCompleteText: Color get() = navigation.menuAutoCompleteText
  override val menuAutoCompleteTextHeader: Color get() = navigation.menuAutoCompleteTextHeader

  // Modal + Mobile + Markdown
  override val modalBackground: Color get() = modalMobile.modalBackground
  override val modalBorder: Color get() = modalMobile.modalBorder
  override val mobileHeaderBackground: Color get() = modalMobile.mobileHeaderBackground
  override val mobileHeaderBackgroundSubdued: Color get() = modalMobile.mobileHeaderBackgroundSubdued
  override val mobileHeaderText: Color get() = modalMobile.mobileHeaderText
  override val mobileHeaderTextTransparent: Color get() = modalMobile.mobileHeaderTextTransparent
  override val mobileHeaderTextSubdued: Color get() = modalMobile.mobileHeaderTextSubdued
  override val mobileHeaderTextHover: Color get() = modalMobile.mobileHeaderTextHover
  override val mobilePageBackground: Color get() = modalMobile.mobilePageBackground
  override val mobileNavBackground: Color get() = modalMobile.mobileNavBackground
  override val mobileNavItem: Color get() = modalMobile.mobileNavItem
  override val mobileNavItemSelected: Color get() = modalMobile.mobileNavItemSelected
  override val mobileAccountShadow: Color get() = modalMobile.mobileAccountShadow
  override val mobileAccountText: Color get() = modalMobile.mobileAccountText
  override val mobileModalBackground: Color get() = modalMobile.mobileModalBackground
  override val mobileModalText: Color get() = modalMobile.mobileModalText
  override val mobileViewTheme: Color get() = modalMobile.mobileViewTheme
  override val mobileConfigServerViewTheme: Color get() = modalMobile.mobileConfigServerViewTheme
  override val markdownNormal: Color get() = modalMobile.markdownNormal
  override val markdownDark: Color get() = modalMobile.markdownDark
  override val markdownLight: Color get() = modalMobile.markdownLight

  // Buttons + Calendar
  override val buttonMenuText: Color get() = button.buttonMenuText
  override val buttonMenuTextHover: Color get() = button.buttonMenuTextHover
  override val buttonMenuBackground: Color get() = button.buttonMenuBackground
  override val buttonMenuBackgroundHover: Color get() = button.buttonMenuBackgroundHover
  override val buttonMenuBorder: Color get() = button.buttonMenuBorder
  override val buttonMenuSelectedText: Color get() = button.buttonMenuSelectedText
  override val buttonMenuSelectedTextHover: Color get() = button.buttonMenuSelectedTextHover
  override val buttonMenuSelectedBackground: Color get() = button.buttonMenuSelectedBackground
  override val buttonMenuSelectedBackgroundHover: Color get() = button.buttonMenuSelectedBackgroundHover
  override val buttonMenuSelectedBorder: Color get() = button.buttonMenuSelectedBorder
  override val buttonPrimaryText: Color get() = button.buttonPrimaryText
  override val buttonPrimaryTextHover: Color get() = button.buttonPrimaryTextHover
  override val buttonPrimaryBackground: Color get() = button.buttonPrimaryBackground
  override val buttonPrimaryBackgroundHover: Color get() = button.buttonPrimaryBackgroundHover
  override val buttonPrimaryBorder: Color get() = button.buttonPrimaryBorder
  override val buttonPrimaryShadow: Color get() = button.buttonPrimaryShadow
  override val buttonPrimaryDisabledText: Color get() = button.buttonPrimaryDisabledText
  override val buttonPrimaryDisabledBackground: Color get() = button.buttonPrimaryDisabledBackground
  override val buttonPrimaryDisabledBorder: Color get() = button.buttonPrimaryDisabledBorder
  override val buttonNormalText: Color get() = button.buttonNormalText
  override val buttonNormalTextHover: Color get() = button.buttonNormalTextHover
  override val buttonNormalBackground: Color get() = button.buttonNormalBackground
  override val buttonNormalBackgroundHover: Color get() = button.buttonNormalBackgroundHover
  override val buttonNormalBorder: Color get() = button.buttonNormalBorder
  override val buttonNormalShadow: Color get() = button.buttonNormalShadow
  override val buttonNormalSelectedText: Color get() = button.buttonNormalSelectedText
  override val buttonNormalSelectedBackground: Color get() = button.buttonNormalSelectedBackground
  override val buttonNormalDisabledText: Color get() = button.buttonNormalDisabledText
  override val buttonNormalDisabledBackground: Color get() = button.buttonNormalDisabledBackground
  override val buttonNormalDisabledBorder: Color get() = button.buttonNormalDisabledBorder
  override val calendarText: Color get() = button.calendarText
  override val calendarBackground: Color get() = button.calendarBackground
  override val calendarItemText: Color get() = button.calendarItemText
  override val calendarItemBackground: Color get() = button.calendarItemBackground
  override val calendarSelectedBackground: Color get() = button.calendarSelectedBackground
  override val calendarCellBackground: Color get() = button.calendarCellBackground
  override val buttonBareText: Color get() = button.buttonBareText
  override val buttonBareTextHover: Color get() = button.buttonBareTextHover
  override val buttonBareBackground: Color get() = button.buttonBareBackground
  override val buttonBareBackgroundHover: Color get() = button.buttonBareBackgroundHover
  override val buttonBareBackgroundActive: Color get() = button.buttonBareBackgroundActive
  override val buttonBareDisabledText: Color get() = button.buttonBareDisabledText
  override val buttonBareDisabledBackground: Color get() = button.buttonBareDisabledBackground

  // Status (notice, warning, error, upcoming, success)
  override val noticeBackground: Color get() = status.noticeBackground
  override val noticeBackgroundLight: Color get() = status.noticeBackgroundLight
  override val noticeBackgroundDark: Color get() = status.noticeBackgroundDark
  override val noticeText: Color get() = status.noticeText
  override val noticeTextLight: Color get() = status.noticeTextLight
  override val noticeTextDark: Color get() = status.noticeTextDark
  override val noticeTextMenu: Color get() = status.noticeTextMenu
  override val noticeBorder: Color get() = status.noticeBorder
  override val warningBackground: Color get() = status.warningBackground
  override val warningText: Color get() = status.warningText
  override val warningTextLight: Color get() = status.warningTextLight
  override val warningTextDark: Color get() = status.warningTextDark
  override val warningBorder: Color get() = status.warningBorder
  override val errorBackground: Color get() = status.errorBackground
  override val errorText: Color get() = status.errorText
  override val errorTextDark: Color get() = status.errorTextDark
  override val errorTextDarker: Color get() = status.errorTextDarker
  override val errorTextMenu: Color get() = status.errorTextMenu
  override val errorBorder: Color get() = status.errorBorder
  override val upcomingBackground: Color get() = status.upcomingBackground
  override val upcomingText: Color get() = status.upcomingText
  override val upcomingBorder: Color get() = status.upcomingBorder
  override val successText: Color get() = status.successText

  // Form + Checkbox
  override val formLabelText: Color get() = form.formLabelText
  override val formLabelBackground: Color get() = form.formLabelBackground
  override val formInputBackground: Color get() = form.formInputBackground
  override val formInputBackgroundSelected: Color get() = form.formInputBackgroundSelected
  override val formInputBackgroundSelection: Color get() = form.formInputBackgroundSelection
  override val formInputBorder: Color get() = form.formInputBorder
  override val formInputTextReadOnlySelection: Color get() = form.formInputTextReadOnlySelection
  override val formInputBorderSelected: Color get() = form.formInputBorderSelected
  override val formInputText: Color get() = form.formInputText
  override val formInputTextSelected: Color get() = form.formInputTextSelected
  override val formInputTextPlaceholder: Color get() = form.formInputTextPlaceholder
  override val formInputTextPlaceholderSelected: Color get() = form.formInputTextPlaceholderSelected
  override val formInputTextSelection: Color get() = form.formInputTextSelection
  override val formInputShadowSelected: Color get() = form.formInputShadowSelected
  override val formInputTextHighlight: Color get() = form.formInputTextHighlight
  override val checkboxText: Color get() = form.checkboxText
  override val checkboxBackgroundSelected: Color get() = form.checkboxBackgroundSelected
  override val checkboxBorderSelected: Color get() = form.checkboxBorderSelected
  override val checkboxShadowSelected: Color get() = form.checkboxShadowSelected
  override val checkboxToggleBackground: Color get() = form.checkboxToggleBackground
  override val checkboxToggleBackgroundSelected: Color get() = form.checkboxToggleBackgroundSelected
  override val checkboxToggleDisabled: Color get() = form.checkboxToggleDisabled

  // Misc (pill, reports, scrollbar, slider, dialog, budgetItem)
  override val pillBackground: Color get() = misc.pillBackground
  override val pillBackgroundLight: Color get() = misc.pillBackgroundLight
  override val pillText: Color get() = misc.pillText
  override val pillTextHighlighted: Color get() = misc.pillTextHighlighted
  override val pillBorder: Color get() = misc.pillBorder
  override val pillBorderDark: Color get() = misc.pillBorderDark
  override val pillBackgroundSelected: Color get() = misc.pillBackgroundSelected
  override val pillTextSelected: Color get() = misc.pillTextSelected
  override val pillBorderSelected: Color get() = misc.pillBorderSelected
  override val reportsRed: Color get() = misc.reportsRed
  override val reportsBlue: Color get() = misc.reportsBlue
  override val reportsGreen: Color get() = misc.reportsGreen
  override val reportsGray: Color get() = misc.reportsGray
  override val reportsLabel: Color get() = misc.reportsLabel
  override val reportsInnerLabel: Color get() = misc.reportsInnerLabel
  override val reportsNumberPositive: Color get() = misc.reportsNumberPositive
  override val reportsNumberNegative: Color get() = misc.reportsNumberNegative
  override val reportsNumberNeutral: Color get() = misc.reportsNumberNeutral
  override val reportsChartFill: Color get() = misc.reportsChartFill
  override val scrollbar: Color get() = misc.scrollbar
  override val scrollbarSelected: Color get() = misc.scrollbarSelected
  override val sliderThumb: Color get() = misc.sliderThumb
  override val sliderActiveTrack: Color get() = misc.sliderActiveTrack
  override val sliderActiveTick: Color get() = misc.sliderActiveTick
  override val sliderInactiveTrack: Color get() = misc.sliderInactiveTrack
  override val sliderInactiveTick: Color get() = misc.sliderInactiveTick
  override val dialogBackground: Color get() = misc.dialogBackground
  override val dialogProgressWheelTrack: Color get() = misc.dialogProgressWheelTrack
  override val budgetItemBackground: Color get() = misc.budgetItemBackground
  override val budgetItemTextPrimary: Color get() = misc.budgetItemTextPrimary
  override val budgetItemTextSecondary: Color get() = misc.budgetItemTextSecondary
}

@Serializable
internal data class PageColors(
  val pageBackground: Color,
  val pageBackgroundModalActive: Color,
  val pageBackgroundTopLeft: Color,
  val pageBackgroundBottomRight: Color,
  val pageBackgroundLineTop: Color,
  val pageBackgroundLineMid: Color,
  val pageBackgroundLineBottom: Color,
  val pageText: Color,
  val pageTextLight: Color,
  val pageTextSubdued: Color,
  val pageTextDark: Color,
  val pageTextPositive: Color,
  val pageTextLink: Color,
  val pageTextLinkLight: Color,
  val numberPositive: Color,
  val numberNegative: Color,
  val numberNeutral: Color,
  val cardBackground: Color,
  val cardBorder: Color,
  val cardShadow: Color,
)

@Serializable
internal data class TableColors(
  val tableBackground: Color,
  val tableRowBackgroundHover: Color,
  val tableText: Color,
  val tableTextLight: Color,
  val tableTextSubdued: Color,
  val tableTextSelected: Color,
  val tableTextHover: Color,
  val tableTextInactive: Color,
  val tableHeaderText: Color,
  val tableHeaderBackground: Color,
  val tableBorder: Color,
  val tableBorderSelected: Color,
  val tableBorderHover: Color,
  val tableBorderSeparator: Color,
  val tableRowBackgroundHighlight: Color,
  val tableRowBackgroundHighlightText: Color,
  val tableRowHeaderBackground: Color,
  val tableRowHeaderText: Color,
)

@Serializable
internal data class NavigationColors(
  val sidebarBackground: Color,
  val sidebarItemBackgroundPositive: Color,
  val sidebarItemBackgroundFailed: Color,
  val sidebarItemAccentSelected: Color,
  val sidebarItemBackgroundHover: Color,
  val sidebarItemText: Color,
  val sidebarItemTextSelected: Color,
  val menuBackground: Color,
  val menuItemBackground: Color,
  val menuItemBackgroundHover: Color,
  val menuItemText: Color,
  val menuItemTextHover: Color,
  val menuItemTextSelected: Color,
  val menuItemTextHeader: Color,
  val menuBorder: Color,
  val menuBorderHover: Color,
  val menuKeybindingText: Color,
  val menuAutoCompleteBackground: Color,
  val menuAutoCompleteBackgroundHover: Color,
  val menuAutoCompleteText: Color,
  val menuAutoCompleteTextHeader: Color,
)

@Serializable
internal data class ModalMobileColors(
  val modalBackground: Color,
  val modalBorder: Color,
  val mobileHeaderBackground: Color,
  val mobileHeaderBackgroundSubdued: Color,
  val mobileHeaderText: Color,
  val mobileHeaderTextTransparent: Color,
  val mobileHeaderTextSubdued: Color,
  val mobileHeaderTextHover: Color,
  val mobilePageBackground: Color,
  val mobileNavBackground: Color,
  val mobileNavItem: Color,
  val mobileNavItemSelected: Color,
  val mobileAccountShadow: Color,
  val mobileAccountText: Color,
  val mobileModalBackground: Color,
  val mobileModalText: Color,
  val mobileViewTheme: Color,
  val mobileConfigServerViewTheme: Color,
  val markdownNormal: Color,
  val markdownDark: Color,
  val markdownLight: Color,
)

@Serializable
internal data class ButtonColors(
  val buttonMenuText: Color,
  val buttonMenuTextHover: Color,
  val buttonMenuBackground: Color,
  val buttonMenuBackgroundHover: Color,
  val buttonMenuBorder: Color,
  val buttonMenuSelectedText: Color,
  val buttonMenuSelectedTextHover: Color,
  val buttonMenuSelectedBackground: Color,
  val buttonMenuSelectedBackgroundHover: Color,
  val buttonMenuSelectedBorder: Color,
  val buttonPrimaryText: Color,
  val buttonPrimaryTextHover: Color,
  val buttonPrimaryBackground: Color,
  val buttonPrimaryBackgroundHover: Color,
  val buttonPrimaryBorder: Color,
  val buttonPrimaryShadow: Color,
  val buttonPrimaryDisabledText: Color,
  val buttonPrimaryDisabledBackground: Color,
  val buttonPrimaryDisabledBorder: Color,
  val buttonNormalText: Color,
  val buttonNormalTextHover: Color,
  val buttonNormalBackground: Color,
  val buttonNormalBackgroundHover: Color,
  val buttonNormalBorder: Color,
  val buttonNormalShadow: Color,
  val buttonNormalSelectedText: Color,
  val buttonNormalSelectedBackground: Color,
  val buttonNormalDisabledText: Color,
  val buttonNormalDisabledBackground: Color,
  val buttonNormalDisabledBorder: Color,
  val calendarText: Color,
  val calendarBackground: Color,
  val calendarItemText: Color,
  val calendarItemBackground: Color,
  val calendarSelectedBackground: Color,
  val calendarCellBackground: Color,
  val buttonBareText: Color,
  val buttonBareTextHover: Color,
  val buttonBareBackground: Color,
  val buttonBareBackgroundHover: Color,
  val buttonBareBackgroundActive: Color,
  val buttonBareDisabledText: Color,
  val buttonBareDisabledBackground: Color,
)

@Serializable
internal data class StatusColors(
  val noticeBackground: Color,
  val noticeBackgroundLight: Color,
  val noticeBackgroundDark: Color,
  val noticeText: Color,
  val noticeTextLight: Color,
  val noticeTextDark: Color,
  val noticeTextMenu: Color,
  val noticeBorder: Color,
  val warningBackground: Color,
  val warningText: Color,
  val warningTextLight: Color,
  val warningTextDark: Color,
  val warningBorder: Color,
  val errorBackground: Color,
  val errorText: Color,
  val errorTextDark: Color,
  val errorTextDarker: Color,
  val errorTextMenu: Color,
  val errorBorder: Color,
  val upcomingBackground: Color,
  val upcomingText: Color,
  val upcomingBorder: Color,
  val successText: Color,
)

@Serializable
internal data class FormColors(
  val formLabelText: Color,
  val formLabelBackground: Color,
  val formInputBackground: Color,
  val formInputBackgroundSelected: Color,
  val formInputBackgroundSelection: Color,
  val formInputBorder: Color,
  val formInputTextReadOnlySelection: Color,
  val formInputBorderSelected: Color,
  val formInputText: Color,
  val formInputTextSelected: Color,
  val formInputTextPlaceholder: Color,
  val formInputTextPlaceholderSelected: Color,
  val formInputTextSelection: Color,
  val formInputShadowSelected: Color,
  val formInputTextHighlight: Color,
  val checkboxText: Color,
  val checkboxBackgroundSelected: Color,
  val checkboxBorderSelected: Color,
  val checkboxShadowSelected: Color,
  val checkboxToggleBackground: Color,
  val checkboxToggleBackgroundSelected: Color,
  val checkboxToggleDisabled: Color,
)

@Serializable
internal data class MiscColors(
  val pillBackground: Color,
  val pillBackgroundLight: Color,
  val pillText: Color,
  val pillTextHighlighted: Color,
  val pillBorder: Color,
  val pillBorderDark: Color,
  val pillBackgroundSelected: Color,
  val pillTextSelected: Color,
  val pillBorderSelected: Color,
  val reportsRed: Color,
  val reportsBlue: Color,
  val reportsGreen: Color,
  val reportsGray: Color,
  val reportsLabel: Color,
  val reportsInnerLabel: Color,
  val reportsNumberPositive: Color,
  val reportsNumberNegative: Color,
  val reportsNumberNeutral: Color,
  val reportsChartFill: Color,
  val scrollbar: Color,
  val scrollbarSelected: Color,
  val sliderThumb: Color,
  val sliderActiveTrack: Color,
  val sliderActiveTick: Color,
  val sliderInactiveTrack: Color,
  val sliderInactiveTick: Color,
  val dialogBackground: Color,
  val dialogProgressWheelTrack: Color,
  val budgetItemBackground: Color,
  val budgetItemTextPrimary: Color,
  val budgetItemTextSecondary: Color,
)
