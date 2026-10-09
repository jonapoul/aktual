package aktual.budget.navrail.ui

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TagId
import aktual.budget.model.WidgetId
import aktual.core.nav.AccountTransactionsNavRoute
import aktual.core.nav.BudgetCategoryNavRoute
import aktual.core.nav.CategoryTransactionsNavRoute
import aktual.core.nav.CreateReportNavRoute
import aktual.core.nav.CreateRuleNavRoute
import aktual.core.nav.CreateScheduleNavRoute
import aktual.core.nav.CreateTagNavRoute
import aktual.core.nav.EditRuleNavRoute
import aktual.core.nav.EditScheduleNavRoute
import aktual.core.nav.EditTagNavRoute
import aktual.core.nav.HomeNavRoute
import aktual.core.nav.ListRulesNavRoute
import aktual.core.nav.ListSchedulesNavRoute
import aktual.core.nav.ListTagsNavRoute
import aktual.core.nav.NavStackImpl
import aktual.core.nav.ReportNavRoute
import aktual.core.nav.ReportsListNavRoute
import aktual.core.nav.SearchReportsNavRoute
import aktual.core.nav.SearchTagsNavRoute
import aktual.core.nav.TransactionsNavRoute
import aktual.core.nav.TransactionsWithTagNavRoute
import aktual.core.nav.UncategorisedTransactionsNavRoute
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.toMutableStateList
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.YearMonth

class BudgetNavKeyStackSaverTest {
  @Test
  fun `Stack with every route type survives saving and restoring`() {
    val keys =
      listOf(
        HomeNavRoute,
        TransactionsNavRoute,
        TransactionsWithTagNavRoute(TagId("tag")),
        AccountTransactionsNavRoute(AccountId("account")),
        UncategorisedTransactionsNavRoute,
        CategoryTransactionsNavRoute(CategoryId("category"), YearMonth(2026, 10)),
        BudgetCategoryNavRoute(CategoryId("category"), YearMonth(2026, 10)),
        ReportsListNavRoute,
        ReportNavRoute(WidgetId("widget")),
        CreateReportNavRoute(DashboardPageId("page")),
        SearchReportsNavRoute,
        ListRulesNavRoute,
        EditRuleNavRoute(RuleId("rule")),
        CreateRuleNavRoute,
        ListSchedulesNavRoute,
        CreateScheduleNavRoute,
        EditScheduleNavRoute(ScheduleId("schedule")),
        ListTagsNavRoute,
        CreateTagNavRoute,
        SearchTagsNavRoute,
        EditTagNavRoute(TagId("tag")),
      )
    val saver = budgetNavKeyStackSaver()
    val stack = NavStackImpl(appCloser = null, stack = keys.toMutableStateList())

    val saved = with(saver) { SaverScope { true }.save(stack) }
    val restored = saver.restore(requireNotNull(saved))

    assertThat(restored?.toList()).isEqualTo(keys)
  }
}
