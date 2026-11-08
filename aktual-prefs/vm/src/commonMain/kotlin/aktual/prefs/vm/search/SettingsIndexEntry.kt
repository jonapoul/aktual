package aktual.prefs.vm.search

import aktual.core.l10n.Res
import aktual.core.l10n.settings_currency
import aktual.core.l10n.settings_currency_add_space
import aktual.core.l10n.settings_currency_default
import aktual.core.l10n.settings_currency_symbol_position
import aktual.core.l10n.settings_dark_theme
import aktual.core.l10n.settings_format_dates
import aktual.core.l10n.settings_format_dates_group
import aktual.core.l10n.settings_format_decimal
import aktual.core.l10n.settings_format_distance
import aktual.core.l10n.settings_format_distance_group
import aktual.core.l10n.settings_format_first_day
import aktual.core.l10n.settings_format_group
import aktual.core.l10n.settings_format_numbers
import aktual.core.l10n.settings_format_numbers_group
import aktual.core.l10n.settings_security
import aktual.core.l10n.settings_security_app_lock
import aktual.core.l10n.settings_security_auto_save_locations
import aktual.core.l10n.settings_security_hide_preview
import aktual.core.l10n.settings_security_locations
import aktual.core.l10n.settings_security_lock_group
import aktual.core.l10n.settings_security_timeout
import aktual.core.l10n.settings_theme
import aktual.core.l10n.settings_theme_built_in
import aktual.core.l10n.settings_theme_custom_title
import aktual.core.l10n.settings_theme_local_title
import aktual.core.l10n.settings_theme_system_group
import aktual.core.l10n.settings_theme_use_system
import aktual.core.l10n.settings_ui_bar_effect
import aktual.core.l10n.settings_ui_bars_title
import aktual.core.l10n.settings_ui_blur_alpha
import aktual.core.l10n.settings_ui_blur_dialogs
import aktual.core.l10n.settings_ui_blur_radius
import aktual.core.l10n.settings_ui_dialogs_title
import aktual.core.l10n.settings_ui_glass_corner_radius
import aktual.core.l10n.settings_ui_glass_style
import aktual.core.l10n.settings_ui_group
import aktual.core.l10n.settings_ui_show_status
import org.jetbrains.compose.resources.StringResource

// Mirrors the layout of the settings screens. An entry with children is a group, one without is a
// single preference. Keep it in sync when adding, moving or removing a setting.
internal data class SettingsIndexEntry(
  val title: StringResource,
  val children: List<SettingsIndexEntry> = emptyList(),
  val needsLocation: Boolean = false,
)

private fun entry(title: StringResource, vararg children: SettingsIndexEntry) =
  SettingsIndexEntry(title, children.toList())

internal val SettingsIndex: Map<SettingsPage, SettingsIndexEntry> =
  mapOf(
    SettingsPage.Theme to
      entry(
        Res.string.settings_theme,
        entry(
          Res.string.settings_theme_system_group,
          entry(Res.string.settings_theme_use_system),
          entry(Res.string.settings_dark_theme),
        ),
        entry(Res.string.settings_theme_built_in),
        entry(Res.string.settings_theme_local_title),
        entry(Res.string.settings_theme_custom_title),
      ),
    SettingsPage.SystemUi to
      entry(
        Res.string.settings_ui_group,
        entry(Res.string.settings_ui_show_status),
        entry(
          Res.string.settings_ui_bars_title,
          entry(Res.string.settings_ui_bar_effect),
          entry(Res.string.settings_ui_blur_radius),
          entry(Res.string.settings_ui_blur_alpha),
          entry(Res.string.settings_ui_glass_style),
          entry(Res.string.settings_ui_glass_corner_radius),
        ),
        entry(
          Res.string.settings_ui_dialogs_title,
          entry(Res.string.settings_ui_blur_dialogs),
          entry(Res.string.settings_ui_blur_radius),
          entry(Res.string.settings_ui_blur_alpha),
        ),
      ),
    SettingsPage.Security to
      entry(
        Res.string.settings_security,
        entry(
          Res.string.settings_security_lock_group,
          entry(Res.string.settings_security_app_lock),
          entry(Res.string.settings_security_timeout),
          entry(Res.string.settings_security_hide_preview),
        ),
        SettingsIndexEntry(
          title = Res.string.settings_security_locations,
          children = listOf(entry(Res.string.settings_security_auto_save_locations)),
          needsLocation = true,
        ),
      ),
    SettingsPage.Format to
      entry(
        Res.string.settings_format_group,
        entry(
          Res.string.settings_format_numbers_group,
          entry(Res.string.settings_format_numbers),
          entry(Res.string.settings_format_decimal),
        ),
        entry(
          Res.string.settings_currency,
          entry(Res.string.settings_currency_default),
          entry(Res.string.settings_currency_symbol_position),
          entry(Res.string.settings_currency_add_space),
        ),
        entry(
          Res.string.settings_format_dates_group,
          entry(Res.string.settings_format_dates),
          entry(Res.string.settings_format_first_day),
        ),
        entry(
          Res.string.settings_format_distance_group,
          entry(Res.string.settings_format_distance),
        ),
      ),
  )
