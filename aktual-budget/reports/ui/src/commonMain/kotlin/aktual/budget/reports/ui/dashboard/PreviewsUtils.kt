package aktual.budget.reports.ui.dashboard

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.charts.PER_TRANSACTION_META
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_META
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_META
import aktual.budget.reports.vm.dashboard.DashboardItem

internal val PREVIEW_DASHBOARD_ITEM_1 =
  DashboardItem(id = WidgetId("abc-123"), x = 0, y = 0, meta = PREVIEW_CASH_FLOW_META)

internal val PREVIEW_DASHBOARD_ITEM_2 =
  DashboardItem(id = WidgetId("def-456"), x = 0, y = 0, meta = PREVIEW_NET_WORTH_META)

internal val PREVIEW_DASHBOARD_ITEM_3 =
  DashboardItem(id = WidgetId("xyz-789"), x = 0, y = 0, meta = PER_TRANSACTION_META)
