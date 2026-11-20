package aktual.prefs.vm.search

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.core.l10n.Res
import aktual.core.l10n.settings_advanced
import aktual.core.l10n.settings_advanced_budget_file
import aktual.core.l10n.settings_advanced_budget_id
import aktual.core.l10n.settings_advanced_repair
import aktual.core.l10n.settings_advanced_reset_sync
import aktual.core.l10n.settings_advanced_sync_id
import aktual.core.l10n.settings_advanced_troubleshooting
import aktual.core.l10n.settings_amounts
import aktual.core.l10n.settings_app_icon
import aktual.core.l10n.settings_currency
import aktual.core.l10n.settings_currency_add_space
import aktual.core.l10n.settings_currency_default
import aktual.core.l10n.settings_currency_symbol_position
import aktual.core.l10n.settings_dark_theme
import aktual.core.l10n.settings_experimental
import aktual.core.l10n.settings_experimental_action_templating
import aktual.core.l10n.settings_experimental_balance_forecast_report
import aktual.core.l10n.settings_experimental_budget_analysis_report
import aktual.core.l10n.settings_experimental_currency
import aktual.core.l10n.settings_experimental_enable_banking
import aktual.core.l10n.settings_experimental_formula_mode
import aktual.core.l10n.settings_experimental_goal_templates
import aktual.core.l10n.settings_experimental_goal_templates_ui
import aktual.core.l10n.settings_experimental_mobile_calculator
import aktual.core.l10n.settings_experimental_monte_carlo_report
import aktual.core.l10n.settings_experimental_new_sidebar
import aktual.core.l10n.settings_experimental_sankey_report
import aktual.core.l10n.settings_experimental_synced
import aktual.core.l10n.settings_format_dates
import aktual.core.l10n.settings_format_dates_group
import aktual.core.l10n.settings_format_decimal
import aktual.core.l10n.settings_format_distance
import aktual.core.l10n.settings_format_distance_group
import aktual.core.l10n.settings_format_first_day
import aktual.core.l10n.settings_format_group
import aktual.core.l10n.settings_format_numbers
import aktual.core.l10n.settings_security
import aktual.core.l10n.settings_security_app_lock
import aktual.core.l10n.settings_security_auto_save_locations
import aktual.core.l10n.settings_security_hide_preview
import aktual.core.l10n.settings_security_locations
import aktual.core.l10n.settings_security_lock_group
import aktual.core.l10n.settings_security_look_up_addresses
import aktual.core.l10n.settings_security_timeout
import aktual.core.l10n.settings_theme
import aktual.core.l10n.settings_theme_catalog
import aktual.core.l10n.settings_theme_list
import aktual.core.l10n.settings_theme_system_group
import aktual.core.l10n.settings_theme_use_system
import aktual.core.l10n.settings_transactions
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
import aktual.core.l10n.settings_ui_start_tab
import aktual.core.l10n.settings_updates
import aktual.core.l10n.settings_updates_nightly
import aktual.core.l10n.transactions_view_alternate_rows
import aktual.core.l10n.transactions_view_expand_splits
import org.jetbrains.compose.resources.StringResource

// Mirrors the layout of the settings screens. An entry with children is a group, one without is a
// single preference. Keep it in sync when adding, moving or removing a setting. An entry with a
// flag, and everything in it, only shows while the open budget has that flag on
internal data class SettingsIndexEntry(
  val title: StringResource,
  val children: List<SettingsIndexEntry> = emptyList(),
  val needsLocation: Boolean = false,
  val flag: FeatureFlag? = null,
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
        entry(Res.string.settings_theme_list, entry(Res.string.settings_theme_catalog)),
      ),
    SettingsPage.SystemUi to
      entry(
        Res.string.settings_ui_group,
        entry(Res.string.settings_ui_start_tab),
        entry(Res.string.settings_ui_show_status),
        entry(Res.string.settings_ui_bars_title),
        entry(Res.string.settings_ui_dialogs_title),
      ),
    SettingsPage.Bars to
      entry(
        Res.string.settings_ui_bars_title,
        entry(Res.string.settings_ui_bar_effect),
        entry(Res.string.settings_ui_blur_radius),
        entry(Res.string.settings_ui_blur_alpha),
        entry(Res.string.settings_ui_glass_style),
        entry(Res.string.settings_ui_glass_corner_radius),
      ),
    SettingsPage.Dialogs to
      entry(
        Res.string.settings_ui_dialogs_title,
        entry(Res.string.settings_ui_blur_dialogs),
        entry(Res.string.settings_ui_blur_radius),
        entry(Res.string.settings_ui_blur_alpha),
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
          children =
            listOf(
              entry(Res.string.settings_security_auto_save_locations),
              entry(Res.string.settings_security_look_up_addresses),
            ),
          needsLocation = true,
        ),
      ),
    SettingsPage.Format to
      entry(
        Res.string.settings_format_group,
        entry(
          Res.string.settings_format_distance_group,
          entry(Res.string.settings_format_distance),
        ),
      ),
    SettingsPage.Dates to
      entry(
        Res.string.settings_format_dates_group,
        entry(Res.string.settings_format_dates),
        entry(Res.string.settings_format_first_day),
      ),
    SettingsPage.Amounts to
      entry(
        Res.string.settings_amounts,
        entry(Res.string.settings_format_numbers),
        entry(Res.string.settings_format_decimal),
        SettingsIndexEntry(
          title = Res.string.settings_currency,
          children =
            listOf(
              entry(Res.string.settings_currency_default),
              entry(Res.string.settings_currency_symbol_position),
              entry(Res.string.settings_currency_add_space),
            ),
          flag = CurrencySupport,
        ),
      ),
    SettingsPage.Transactions to
      entry(
        Res.string.settings_transactions,
        entry(Res.string.transactions_view_alternate_rows),
        entry(Res.string.transactions_view_expand_splits),
      ),
    SettingsPage.LauncherIcon to entry(Res.string.settings_app_icon),
    SettingsPage.Updates to
      entry(Res.string.settings_updates, entry(Res.string.settings_updates_nightly)),
    SettingsPage.Advanced to
      entry(
        Res.string.settings_advanced,
        entry(
          Res.string.settings_advanced_budget_file,
          entry(Res.string.settings_advanced_budget_id),
          entry(Res.string.settings_advanced_sync_id),
        ),
        entry(
          Res.string.settings_advanced_troubleshooting,
          entry(Res.string.settings_advanced_reset_sync),
          entry(Res.string.settings_advanced_repair),
        ),
      ),
    SettingsPage.Experimental to
      entry(
        Res.string.settings_experimental,
        SettingsIndexEntry(
          title = Res.string.settings_experimental_synced,
          children =
            FeatureFlag.entries.map { flag ->
              // The budget automations UI flag only shows once goal templates are on
              SettingsIndexEntry(
                title = flag.title,
                flag = if (flag == GoalTemplatesUi) GoalTemplates else null,
              )
            },
        ),
      ),
  )

private val FeatureFlag.title: StringResource
  get() =
    when (this) {
      GoalTemplates -> Res.string.settings_experimental_goal_templates
      GoalTemplatesUi -> Res.string.settings_experimental_goal_templates_ui
      ActionTemplating -> Res.string.settings_experimental_action_templating
      FormulaMode -> Res.string.settings_experimental_formula_mode
      CurrencySupport -> Res.string.settings_experimental_currency
      MobileCalculator -> Res.string.settings_experimental_mobile_calculator
      NewSidebarUi -> Res.string.settings_experimental_new_sidebar
      SankeyReport -> Res.string.settings_experimental_sankey_report
      BalanceForecastReport -> Res.string.settings_experimental_balance_forecast_report
      BudgetAnalysisReport -> Res.string.settings_experimental_budget_analysis_report
      MonteCarloReport -> Res.string.settings_experimental_monte_carlo_report
      EnableBanking -> Res.string.settings_experimental_enable_banking
    }
