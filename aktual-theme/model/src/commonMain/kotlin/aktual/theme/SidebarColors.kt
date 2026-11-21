package aktual.theme

import androidx.compose.runtime.Immutable

/**
 * The colours to draw the sidebar with. Like upstream's sidebar-redesign-light.css, only the
 * built-in light theme swaps its sidebar colours for the `sidebarRedesign*` ones.
 */
fun Colors.sidebarColors(): Colors = if (this == LightColors) LightSidebarColors else this

/** [LightColors] with the redesigned sidebar's palette. */
@Immutable
data object LightSidebarColors : Colors by LightColors {
  override val sidebarBackground = LightColors.sidebarRedesignBackground
  override val sidebarItemText = LightColors.sidebarRedesignItemText
  override val sidebarItemTextUpdated = sidebarItemText
  override val sidebarItemBackgroundHover = LightColors.sidebarRedesignItemBackgroundHover
  override val sidebarItemAccentSelected = LightColors.sidebarRedesignItemAccentSelected
  override val sidebarItemTextSelected = LightColors.sidebarRedesignItemTextSelected
  override val sidebarItemBackgroundSelected = LightColors.sidebarRedesignItemBackgroundSelected
  override val sidebarItemBackgroundFailed = LightColors.sidebarRedesignItemBackgroundFailed
  override val sidebarHeaderText = LightColors.sidebarRedesignHeaderText
  override val sidebarTextSubdued = LightColors.sidebarRedesignTextSubdued
  override val sidebarTextMuted = LightColors.sidebarRedesignTextMuted
  override val sidebarTextFailed = sidebarItemBackgroundFailed
  override val sidebarBackgroundFailedSubtle = sidebarItemBackgroundFailed.withOpacity(0.16)
  override val sidebarBorder = sidebarItemBackgroundHover
  override val sidebarControlBackground = sidebarItemBackgroundHover
  override val sidebarBrand = sidebarItemAccentSelected
}
