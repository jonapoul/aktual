package aktual.budget.reports.vm

import aktual.budget.db.CustomReportAccounts
import aktual.budget.db.CustomReportAmounts
import aktual.budget.db.CustomReportCategories
import aktual.budget.db.CustomReportCategoryGroups
import aktual.budget.db.CustomReportPayees
import aktual.budget.db.CustomReportTags
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.PayeeId
import aktual.budget.model.TagId
import aktual.budget.reports.vm.CustomGroup.Uncategorized
import aktual.budget.reports.vm.CustomGroup.UncategorizedAndOffBudget
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

class CustomSpreadsheetTest {
  @Test
  fun `Monthly payments by category`() {
    val sheet =
      spreadsheet(
        amounts =
          listOf(
            row(JAN, category = FOOD, debts = -1000),
            row(JAN, category = FOOD, debts = -500, payee = SHOP),
            row(FEB, category = FUEL, debts = -2000),
            row(FEB, category = SALARY, assets = 50_000),
          ),
      )

    assertThat(sheet.groups.map { it.group }).containsExactly(FUEL_GROUP, FOOD_GROUP)
    assertThat(sheet.groups.map { it.totals.debts }).containsExactly(Amount(-2000), Amount(-1500))
    assertThat(sheet.intervals.map { it.interval.start }).containsExactly(date(JAN), date(FEB))
    assertThat(sheet.intervals[0].stacked[FOOD_GROUP]).isEqualTo(Amount(1500))
    assertThat(sheet.intervals[1].stacked[FUEL_GROUP]).isEqualTo(Amount(2000))
    assertThat(sheet.intervals[1].totals)
      .isEqualTo(totals(assets = 50_000, debts = -2000, netAssets = 48_000, netDebts = 0))
    assertThat(sheet.totals)
      .isEqualTo(totals(assets = 50_000, debts = -3500, netAssets = 48_000, netDebts = -1500))
  }

  @Test
  fun `Monthly intervals are cut to the report's dates`() {
    val sheet = spreadsheet(range = date("2026-01-15")..date("2026-03-10"))
    assertThat(sheet.intervals.map { it.interval })
      .containsExactly(
        CustomInterval(date(JAN), date("2026-01-15")..date("2026-01-31")),
        CustomInterval(date(FEB), date("2026-02-01")..date("2026-02-28")),
        CustomInterval(date(MAR), date("2026-03-01")..date("2026-03-10")),
      )
  }

  @Test
  fun `Amounts without a category go to the made-up categories`() {
    val sheet =
      spreadsheet(
        config = CONFIG.copy(showOffBudget = true),
        amounts =
          listOf(
            row(JAN, debts = -100),
            row(JAN, debts = -200, transferAccount = SAVINGS),
            row(JAN, debts = -400, offBudget = true),
            row(JAN, category = FOOD, debts = -800, offBudget = true),
          ),
      )

    assertThat(sheet.groups.associate { it.group to it.totals.debts })
      .isEqualTo(
        mapOf(
          CustomGroup.OffBudget to Amount(-1200),
          CustomGroup.Transfers to Amount(-200),
          Uncategorized to Amount(-100),
        ),
      )
  }

  @Test
  fun `Hidden, off budget and uncategorised amounts are left out unless asked for`() {
    val amounts =
      listOf(
        row(JAN, category = FOOD, debts = -100),
        row(JAN, category = FUEL, debts = -200, categoryHidden = true),
        row(JAN, category = SALARY, debts = -400, groupHidden = true),
        row(JAN, debts = -800, offBudget = true),
        row(JAN, debts = -1600),
      )

    val hiding =
      spreadsheet(
        config = CONFIG.copy(showHidden = false, showOffBudget = false, showUncategorized = false),
        amounts = amounts,
      )
    assertThat(hiding.totals.debts).isEqualTo(Amount(-100))

    val showing =
      spreadsheet(
        config = CONFIG.copy(showHidden = true, showOffBudget = true, showUncategorized = true),
        amounts = amounts,
      )
    assertThat(showing.totals.debts).isEqualTo(Amount(-3100))
  }

  @Test
  fun `Empty groups are only shown when asked for`() {
    val amounts = listOf(row(JAN, category = FOOD, debts = -100))

    assertThat(spreadsheet(amounts = amounts).groups.map { it.group }).containsExactly(FOOD_GROUP)
    assertThat(spreadsheet(config = CONFIG.copy(showEmpty = true), amounts = amounts).groups.size)
      .isEqualTo(CATEGORIES.size + 3)
  }

  @Test
  fun `Net shows groups whose amounts cancel out`() {
    val sheet =
      spreadsheet(
        config = CONFIG.copy(balanceType = Net),
        amounts =
          listOf(
            row(JAN, category = FOOD, assets = 100, debts = -100),
            row(JAN, category = FUEL, assets = 300),
          ),
      )

    assertThat(sheet.groups.map { it.group }).containsExactly(FUEL_GROUP, FOOD_GROUP)
    assertThat(sheet.groups.map { it.totals.total }).containsExactly(Amount(300), Amount.Zero)
  }

  @Test
  fun `Payments sort by the most spent, deposits by the most received`() {
    val amounts =
      listOf(
        row(JAN, category = FOOD, debts = -100, assets = 900),
        row(JAN, category = FUEL, debts = -300, assets = 100),
      )

    assertThat(spreadsheet(amounts = amounts).groups.map { it.group })
      .containsExactly(FUEL_GROUP, FOOD_GROUP)
    assertThat(
        spreadsheet(config = CONFIG.copy(balanceType = Deposit), amounts = amounts).groups.map {
          it.group
        },
      )
      .containsExactly(FOOD_GROUP, FUEL_GROUP)
    assertThat(
        spreadsheet(config = CONFIG.copy(sortBy = Asc), amounts = amounts).groups.map {
          it.group
        },
      )
      .containsExactly(FOOD_GROUP, FUEL_GROUP)
    assertThat(
        spreadsheet(config = CONFIG.copy(sortBy = Name), amounts = amounts).groups.map {
          it.group
        },
      )
      .containsExactly(FOOD_GROUP, FUEL_GROUP)
  }

  @Test
  fun `Trimming drops the empty intervals at either end`() {
    val config = CONFIG.copy(trimIntervals = true)
    val sheet =
      spreadsheet(
        config = config,
        range = date(JAN)..date("2026-04-30"),
        amounts = listOf(row(FEB, category = FOOD, debts = -100)),
      )

    assertThat(sheet.intervals.map { it.interval.start }).containsExactly(date(FEB))
    assertThat(sheet.groups.single().intervals.map { it.interval.start }).containsExactly(date(FEB))
    assertThat(sheet.intervalCount).isEqualTo(4)
  }

  @Test
  fun `Trimming with nothing to show leaves no intervals`() {
    val sheet = spreadsheet(config = CONFIG.copy(trimIntervals = true))
    assertThat(sheet.intervals).isEmpty()
  }

  @Test
  fun `Group intervals track the change from the last one`() {
    val sheet =
      spreadsheet(
        amounts =
          listOf(
            row(JAN, category = FOOD, debts = -100),
            row(FEB, category = FOOD, debts = -300),
          ),
      )

    assertThat(sheet.groups.single().intervals.map { it.change })
      .containsExactly(Amount.Zero, Amount(-200))
  }

  @Test
  fun `Trend lines fit each group's stacked amounts`() {
    val sheet =
      spreadsheet(
        config = CONFIG.copy(showTrendLines = true),
        range = date(JAN)..date("2026-03-31"),
        amounts =
          listOf(
            row(JAN, category = FOOD, debts = -100),
            row(FEB, category = FOOD, debts = -300),
            row(MAR, category = FOOD, debts = -200),
            row(FEB, category = FUEL, debts = -600),
          ),
      )

    assertThat(sheet.trends)
      .isEqualTo(
        mapOf(
          FOOD_GROUP to CustomTrend(start = Amount(150), end = Amount(250)),
          FUEL_GROUP to CustomTrend(start = Amount(200), end = Amount(200)),
        ),
      )
  }

  @Test
  fun `Trend lines are only fitted when the report shows them`() {
    val amounts = listOf(row(JAN, category = FOOD, debts = -100))
    assertThat(spreadsheet(amounts = amounts).trends).isEmpty()

    val oneInterval =
      spreadsheet(
        config = CONFIG.copy(showTrendLines = true),
        range = date(JAN)..date("2026-01-31"),
        amounts = amounts,
      )
    assertThat(oneInterval.trends).isEmpty()
  }

  @Test
  fun `Weekly amounts are grouped by the week they fall in`() {
    val config = CONFIG.copy(interval = Weekly)
    // 2026-01-04 is a Sunday
    val sheet =
      spreadsheet(
        config = config,
        range = date("2026-01-01")..date("2026-01-14"),
        firstDayOfWeek = Sunday,
        amounts =
          listOf(
            row(20260103, category = FOOD, debts = -100),
            row(20260104, category = FOOD, debts = -200),
            row(20260110, category = FOOD, debts = -400),
          ),
      )

    assertThat(sheet.intervals.map { it.interval })
      .containsExactly(
        CustomInterval(date("2025-12-28"), date("2026-01-01")..date("2026-01-03")),
        CustomInterval(date("2026-01-04"), date("2026-01-04")..date("2026-01-10")),
        CustomInterval(date("2026-01-11"), date("2026-01-11")..date("2026-01-14")),
      )
    assertThat(sheet.intervals.map { it.totals.debts })
      .containsExactly(Amount(-100), Amount(-600), Amount.Zero)
  }

  @Test
  fun `Yearly amounts`() {
    val config = CONFIG.copy(interval = Yearly)
    val sheet =
      spreadsheet(
        config = config,
        range = date("2025-06-01")..date("2026-02-01"),
        amounts =
          listOf(
            row(2025, category = FOOD, debts = -100),
            row(2026, category = FOOD, debts = -200),
          ),
      )

    assertThat(sheet.intervals.map { it.interval.start })
      .containsExactly(date("2025-01-01"), date("2026-01-01"))
    assertThat(sheet.intervals.map { it.totals.debts }).containsExactly(Amount(-100), Amount(-200))
  }

  @Test
  fun `By category group, with each group's categories`() {
    val sheet =
      spreadsheet(
        config = CONFIG.copy(groupBy = Group),
        amounts =
          listOf(
            row(JAN, category = FOOD, debts = -100),
            row(JAN, category = FUEL, debts = -200),
            row(JAN, debts = -400),
          ),
      )

    assertThat(sheet.groups.map { it.group })
      .containsExactly(UncategorizedAndOffBudget, SPENDING_GROUP)
    assertThat(sheet.categoryGroups.map { it.group })
      .containsExactly(UncategorizedAndOffBudget, SPENDING_GROUP)
    assertThat(sheet.categoryGroups[0].categories.map { it.group }).containsExactly(Uncategorized)
    assertThat(sheet.categoryGroups[1].categories.map { it.group })
      .containsExactly(FUEL_GROUP, FOOD_GROUP)
  }

  @Test
  fun `By payee`() {
    val sheet =
      spreadsheet(
        config = CONFIG.copy(groupBy = Payee),
        amounts =
          listOf(
            row(JAN, category = FOOD, debts = -100, payee = SHOP),
            row(JAN, category = FUEL, debts = -200),
          ),
      )

    assertThat(sheet.groups.map { it.group }).containsExactly(CustomGroup.Payee(SHOP, "Shop"))
  }

  @Test
  fun `By account`() {
    val sheet =
      spreadsheet(
        config = CONFIG.copy(groupBy = Account),
        amounts = listOf(row(JAN, category = FOOD, debts = -100, account = SAVINGS)),
      )

    assertThat(sheet.groups.map { it.group })
      .containsExactly(CustomGroup.Account(SAVINGS, "Savings"))
  }

  @Test
  fun `Grouping by payee with no payees is empty`() {
    val sheet =
      spreadsheet(
        config = CONFIG.copy(groupBy = Payee),
        amounts = listOf(row(JAN, category = FOOD, debts = -100)),
        payees = emptyList(),
      )

    assertThat(sheet.intervals).isEmpty()
    assertThat(sheet.totals).isEqualTo(Zero)
  }

  @Test
  fun `Splitting by tag buckets each transaction by the exact set of tags in its notes`() {
    val sheet =
      spreadsheet(
        config = CONFIG.copy(groupBy = Tag),
        tagged =
          listOf(
            tagged(row(JAN, category = FOOD, debts = -100), "Lunch #work"),
            tagged(row(FEB, category = FOOD, debts = -200), "#work ##escaped #trip #unknown"),
            tagged(row(FEB, category = FUEL, debts = -400), "#trip #work"),
            tagged(row(FEB, category = FUEL, debts = -800), "#Work is a different tag"),
            tagged(row(FEB, category = FUEL, debts = -1600), null),
          ),
      )

    assertThat(sheet.groups.associate { it.group to it.totals.debts })
      .isEqualTo(
        mapOf(
          WORK_BUCKET to Amount(-100),
          TRIP_AND_WORK_BUCKET to Amount(-600),
          CustomGroup.Untagged to Amount(-2400),
        ),
      )
  }

  @Test
  fun `A tag scope ignores the other tags, and hidden tags are never in scope`() {
    val sheet =
      spreadsheet(
        config =
          CONFIG.copy(groupBy = Tag, tagScope = CustomTagScope.Selected(persistentListOf(TRIP))),
        tags = TAGS + CustomReportTags(TagId("hidden"), "hidden", true),
        tagged =
          listOf(
            tagged(row(JAN, category = FOOD, debts = -100), "#work"),
            tagged(row(JAN, category = FOOD, debts = -200), "#trip #work #hidden"),
          ),
      )

    assertThat(sheet.groups.associate { it.group to it.totals.debts })
      .isEqualTo(
        mapOf(
          CustomGroup.TagSet(persistentListOf(TRIP), persistentListOf("trip")) to Amount(-200),
          CustomGroup.Untagged to Amount(-100),
        ),
      )
  }

  @Test
  fun `Splitting by tag with empty rows lists every tag and the untagged bucket`() {
    val sheet = spreadsheet(config = CONFIG.copy(groupBy = Tag, showEmpty = true))

    assertThat(sheet.groups.map { it.group })
      .containsExactly(
        WORK_BUCKET,
        CustomGroup.TagSet(persistentListOf(TRIP), persistentListOf("trip")),
        CustomGroup.Untagged,
      )
  }

  @Test
  fun `Tags in a bucket are ordered by name, numbers by value`() {
    val tags =
      listOf(
        CustomReportTags(TagId("b10"), "b10", false),
        CustomReportTags(TagId("b9"), "b9", false),
        CustomReportTags(TagId("a"), "A", false),
      )
    val sheet =
      spreadsheet(
        config = CONFIG.copy(groupBy = Tag),
        tags = tags,
        tagged = listOf(tagged(row(JAN, category = FOOD, debts = -100), "#b10 #A #b9")),
      )

    assertThat(sheet.groups.single().group)
      .isEqualTo(
        CustomGroup.TagSet(
          persistentListOf(TagId("a"), TagId("b9"), TagId("b10")),
          persistentListOf("A", "b9", "b10"),
        ),
      )
  }

  @Test
  fun `Monthly and yearly queries cover whole months and years`() {
    val range = date("2026-01-15")..date("2026-03-10")
    assertThat(customReportQuery(Monthly, range))
      .isEqualTo(CustomReportQuery(date("2026-01-01"), date("2026-03-31"), divisor = 100))
    assertThat(customReportQuery(Yearly, range))
      .isEqualTo(CustomReportQuery(date("2026-01-01"), date("2026-12-31"), divisor = 10_000))
    assertThat(customReportQuery(Daily, range))
      .isEqualTo(CustomReportQuery(date("2026-01-15"), date("2026-03-10"), divisor = 1))
  }

  private fun spreadsheet(
    config: CustomReportConfig = CONFIG,
    range: LocalDateRange = date(JAN)..date("2026-02-28"),
    firstDayOfWeek: FirstDayOfWeek = Sunday,
    amounts: List<CustomReportAmounts> = emptyList(),
    payees: List<CustomReportPayees> = listOf(CustomReportPayees(SHOP, "Shop")),
    tagged: List<TaggedAmounts> = emptyList(),
    tags: List<CustomReportTags> = TAGS,
  ) =
    customSpreadsheet(
      config = config,
      range = range,
      firstDayOfWeek = firstDayOfWeek,
      inputs =
        CustomReportInputs(
          amounts = amounts,
          categories = CATEGORIES,
          categoryGroups = GROUPS,
          payees = payees,
          accounts =
            listOf(
              CustomReportAccounts(CHECKING, "Checking"),
              CustomReportAccounts(SAVINGS, "Savings"),
            ),
          tagged = tagged,
          tags = tags,
        ),
    )

  private companion object {
    const val JAN = 202601L
    const val FEB = 202602L
    const val MAR = 202603L

    val SPENDING = CategoryGroupId("spending")
    val INCOME = CategoryGroupId("income")
    val FOOD = CategoryId("food")
    val FUEL = CategoryId("fuel")
    val SALARY = CategoryId("salary")
    val CHECKING = AccountId("checking")
    val SAVINGS = AccountId("savings")
    val SHOP = PayeeId("shop")
    val WORK = TagId("work")
    val TRIP = TagId("trip")

    val TAGS = listOf(CustomReportTags(WORK, "work", false), CustomReportTags(TRIP, "trip", null))
    val WORK_BUCKET = CustomGroup.TagSet(persistentListOf(WORK), persistentListOf("work"))
    val TRIP_AND_WORK_BUCKET =
      CustomGroup.TagSet(persistentListOf(TRIP, WORK), persistentListOf("trip", "work"))

    val FOOD_GROUP = CustomGroup.Category(FOOD, "Food")
    val FUEL_GROUP = CustomGroup.Category(FUEL, "Fuel")
    val SPENDING_GROUP = CustomGroup.Group(SPENDING, "Spending")

    val CATEGORIES =
      listOf(
        CustomReportCategories(FOOD, "Food", SPENDING, 1.0, false, false),
        CustomReportCategories(FUEL, "Fuel", SPENDING, 2.0, false, false),
        CustomReportCategories(SALARY, "Salary", INCOME, 1.0, false, false),
      )
    val GROUPS =
      listOf(
        CustomReportCategoryGroups(SPENDING, "Spending"),
        CustomReportCategoryGroups(INCOME, "Income"),
      )

    val CONFIG =
      checkNotNull(emptyCustomReport().toConfig())
        .copy(
          showHidden = true,
          showUncategorized = true,
          dates = CustomReportDates.Live(Last3Months, includeCurrent = true),
        )

    fun date(string: String) = LocalDate.parse(string)

    // A month key like 202601 to its first day
    fun date(month: Long) = LocalDate((month / 100).toInt(), (month % 100).toInt(), 1)

    fun totals(assets: Long, debts: Long, netAssets: Long, netDebts: Long) =
      CustomTotals(
        assets = Amount(assets),
        debts = Amount(debts),
        netAssets = Amount(netAssets),
        netDebts = Amount(netDebts),
        total = Amount(assets + debts),
      )

    fun tagged(amounts: CustomReportAmounts, notes: String?) = TaggedAmounts(amounts, notes)

    fun row(
      key: Long,
      category: CategoryId? = null,
      assets: Long = 0,
      debts: Long = 0,
      account: AccountId = CHECKING,
      payee: PayeeId? = null,
      transferAccount: AccountId? = null,
      offBudget: Boolean = false,
      categoryHidden: Boolean = false,
      groupHidden: Boolean = false,
    ) =
      CustomReportAmounts(
        interval_key = key,
        category = category,
        category_hidden = category?.let { categoryHidden },
        category_group = CATEGORIES.firstOrNull { it.id == category }?.cat_group,
        category_group_hidden = category?.let { groupHidden },
        account = account,
        off_budget = offBudget,
        payee = payee,
        transfer_account = transferAccount,
        assets = assets,
        debts = debts,
      )
  }
}
