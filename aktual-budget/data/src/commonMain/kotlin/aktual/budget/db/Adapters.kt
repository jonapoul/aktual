package aktual.budget.db

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.BalanceType
import aktual.budget.model.BankId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.CustomReportId
import aktual.budget.model.CustomReportMode
import aktual.budget.model.DateRangeType
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import aktual.budget.model.ReportCondition
import aktual.budget.model.ReportDate
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleJsonPathIndex
import aktual.budget.model.ScheduleNextDateId
import aktual.budget.model.SelectedCategory
import aktual.budget.model.SortBy
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.Timestamp
import aktual.budget.model.TransactionFilterId
import aktual.budget.model.TransactionId
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.model.ZeroBudgetMonthId
import alakazam.kotlin.parse
import app.cash.sqldelight.ColumnAdapter
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

private fun <T : Any> longAdapter(
    decode: (Long) -> T,
    encode: (T) -> Long,
): ColumnAdapter<T, Long> =
    object : ColumnAdapter<T, Long> {
      override fun encode(value: T): Long = encode.invoke(value)

      override fun decode(databaseValue: Long): T = decode.invoke(databaseValue)
    }

private fun <T : Any> stringAdapter(
    decode: (String) -> T,
    encode: (T) -> String,
): ColumnAdapter<T, String> =
    object : ColumnAdapter<T, String> {
      override fun encode(value: T): String = encode.invoke(value)

      override fun decode(databaseValue: String): T = decode.invoke(databaseValue)
    }

private fun <T : Any> stringAdapter(decode: (String) -> T): ColumnAdapter<T, String> =
    stringAdapter(decode, encode = { it.toString() })

private inline fun <reified E : Enum<E>> enumStringAdapter(): ColumnAdapter<E, String> =
    stringAdapter {
      E::class.parse(it)
    }

private inline fun <reified T : JsonElement> jsonElement(
    crossinline getter: JsonElement.() -> T,
) =
    object : ColumnAdapter<T, String> {
      override fun encode(value: T): String = Json.encodeToString(value)

      override fun decode(databaseValue: String): T =
          Json.parseToJsonElement(databaseValue).getter()
    }

private val jsonElement = jsonElement<JsonElement> { this }
private val jsonObject = jsonElement<JsonObject> { jsonObject }
private val jsonArray = jsonElement<JsonArray> { jsonArray }

private inline fun <reified T : Any> jsonSerializable(serializer: KSerializer<T>) =
    object : ColumnAdapter<T, String> {
      override fun decode(databaseValue: String): T =
          Json.decodeFromString(serializer, databaseValue)

      override fun encode(value: T): String = Json.encodeToString(serializer, value)
    }

private val reportConditions = jsonSerializable(ReportCondition.ListSerializer)
private val selectedCategories = jsonSerializable(ListSerializer(SelectedCategory.serializer()))

private val localDate =
    longAdapter(
        encode = { date: LocalDate ->
          with(date) { "%04d%02d%02d".format(year, month.number, day).toLong() }
        },
        decode = { value: Long ->
          val str = value.toString()
          val year = str.substring(startIndex = 0, endIndex = 4).toInt()
          val month = str.substring(startIndex = 4, endIndex = 6).toInt()
          val day = str.substring(startIndex = 6, endIndex = 8).toInt()
          LocalDate(year, month, day)
        },
    )

private val instantMsFromString =
    stringAdapter(
        encode = { it.toEpochMilliseconds().toString() },
        decode = { Instant.fromEpochMilliseconds(it.toLong()) },
    )
private val amount = longAdapter(::Amount, Amount::toLong)
private val instantFromLong =
    longAdapter(Instant::fromEpochMilliseconds, Instant::toEpochMilliseconds)

private const val YEAR_MONTH_FACTOR = 100
private val yearMonth =
    longAdapter(
        decode = { long ->
          val month = Month(long.toInt() % YEAR_MONTH_FACTOR)
          val year = long.toInt() / YEAR_MONTH_FACTOR
          YearMonth(year, month)
        },
        encode = { (it.year * YEAR_MONTH_FACTOR.toLong()) + it.month.number },
    )

private val accountId = stringAdapter(::AccountId)
private val accountSyncSource = stringAdapter(AccountSyncSource::fromString)
private val bankId = stringAdapter(::BankId)
private val syncedPrefKey = stringAdapter(SyncedPrefKey::decode, SyncedPrefKey::key)
private val categoryGroupId = stringAdapter(::CategoryGroupId)
private val categoryId = stringAdapter(::CategoryId)
private val customReportsId = stringAdapter(::CustomReportId)
private val payeeId = stringAdapter(::PayeeId)
private val reportDate = stringAdapter(ReportDate::parse)
private val ruleId = stringAdapter(::RuleId)
private val scheduleId = stringAdapter(::ScheduleId)
private val scheduleJsonPathIndex = stringAdapter(::ScheduleJsonPathIndex)
private val scheduleNextDateId = stringAdapter(::ScheduleNextDateId)
private val timestamp = stringAdapter(Timestamp::parse)
private val transactionFilterId = stringAdapter(::TransactionFilterId)
private val transactionId = stringAdapter(::TransactionId)
private val widgetId = stringAdapter(::WidgetId)
private val uuid = stringAdapter(Uuid::parse)
private val zeroBudgetMonthId = stringAdapter(::ZeroBudgetMonthId)

private val balanceType = enumStringAdapter<BalanceType>()
private val operator = enumStringAdapter<Operator>()
private val customReportMode = enumStringAdapter<CustomReportMode>()
private val dateRangeType = enumStringAdapter<DateRangeType>()
private val graphType = enumStringAdapter<GraphType>()
private val groupBy = enumStringAdapter<GroupBy>()
private val interval = enumStringAdapter<Interval>()
private val sortBy = enumStringAdapter<SortBy>()
private val ruleStage = enumStringAdapter<RuleStage>()
private val widgetType = enumStringAdapter<WidgetType>()

internal val AccountsAdapter =
    Accounts.Adapter(
        idAdapter = accountId,
        balance_currentAdapter = amount,
        balance_availableAdapter = amount,
        balance_limitAdapter = amount,
        bankAdapter = uuid,
        account_sync_sourceAdapter = accountSyncSource,
        last_syncAdapter = instantMsFromString,
        last_reconciledAdapter = instantMsFromString,
    )

internal val BanksAdapter =
    Banks.Adapter(
        idAdapter = uuid,
        bank_idAdapter = bankId,
    )

internal val DashboardAdapter =
    Dashboard.Adapter(
        idAdapter = widgetId,
        typeAdapter = widgetType,
        metaAdapter = jsonObject,
    )

internal val PayeesAdapter =
    Payees.Adapter(
        idAdapter = payeeId,
        transfer_acctAdapter = accountId,
    )

internal val TransactionsAdapter =
    Transactions.Adapter(
        acctAdapter = accountId,
        amountAdapter = amount,
        idAdapter = transactionId,
        categoryAdapter = categoryId,
        dateAdapter = localDate,
        errorAdapter = jsonObject,
        imported_descriptionAdapter = payeeId,
        transferred_idAdapter = transactionId,
        parent_idAdapter = transactionId,
        scheduleAdapter = scheduleId,
        descriptionAdapter = payeeId,
    )

internal val CategoriesAdapter =
    Categories.Adapter(
        idAdapter = categoryId,
        cat_groupAdapter = categoryGroupId,
        goal_defAdapter = jsonElement,
    )

internal val CategoryGroupsAdapter =
    Category_groups.Adapter(
        idAdapter = categoryGroupId,
    )

internal val CategoryMappingAdapter =
    Category_mapping.Adapter(
        idAdapter = categoryId,
        transferIdAdapter = categoryId,
    )

internal val CustomReportsAdapter =
    Custom_reports.Adapter(
        idAdapter = customReportsId,
        start_dateAdapter = reportDate,
        end_dateAdapter = reportDate,
        date_rangeAdapter = dateRangeType,
        modeAdapter = customReportMode,
        group_byAdapter = groupBy,
        balance_typeAdapter = balanceType,
        graph_typeAdapter = graphType,
        conditionsAdapter = reportConditions,
        conditions_opAdapter = operator,
        metadataAdapter = jsonObject,
        sort_byAdapter = sortBy,
        intervalAdapter = interval,
        selected_categoriesAdapter = selectedCategories,
    )

internal val MessagesClockAdapter =
    Messages_clock.Adapter(
        clockAdapter = jsonObject,
    )

internal val MessagesCrdtAdapter =
    Messages_crdt.Adapter(
        timestampAdapter = timestamp,
    )

internal val PayeeMappingAdapter =
    Payee_mapping.Adapter(
        idAdapter = payeeId,
        targetIdAdapter = payeeId,
    )

internal val PreferencesAdapter =
    Preferences.Adapter(
        idAdapter = syncedPrefKey,
    )

internal val RulesAdapter =
    Rules.Adapter(
        idAdapter = ruleId,
        stageAdapter = ruleStage,
        conditionsAdapter = jsonArray,
        actionsAdapter = jsonArray,
        conditions_opAdapter = operator,
    )

internal val SchedulesAdapter =
    Schedules.Adapter(
        idAdapter = scheduleId,
        ruleAdapter = ruleId,
    )

internal val SchedulesJsonPathsAdapter =
    Schedules_json_paths.Adapter(
        schedule_idAdapter = scheduleId,
        payeeAdapter = scheduleJsonPathIndex,
        accountAdapter = scheduleJsonPathIndex,
        amountAdapter = scheduleJsonPathIndex,
        dateAdapter = scheduleJsonPathIndex,
    )

internal val SchedulesNextDateAdapter =
    Schedules_next_date.Adapter(
        idAdapter = scheduleNextDateId,
        schedule_idAdapter = scheduleId,
        local_next_dateAdapter = localDate,
        local_next_date_tsAdapter = instantFromLong,
        base_next_dateAdapter = localDate,
        base_next_date_tsAdapter = instantFromLong,
    )

internal val ReflectBudgetsAdapter =
    Reflect_budgets.Adapter(
        monthAdapter = yearMonth,
        categoryAdapter = categoryId,
        amountAdapter = amount,
    )

internal val TransactionFiltersAdapter =
    Transaction_filters.Adapter(
        idAdapter = transactionFilterId,
        conditionsAdapter = jsonArray,
        conditions_opAdapter = operator,
    )

internal val ZeroBudgetMonthsAdapter =
    Zero_budget_months.Adapter(
        idAdapter = zeroBudgetMonthId,
    )

internal val ZeroBudgetsAdapter =
    Zero_budgets.Adapter(
        monthAdapter = yearMonth,
        categoryAdapter = categoryId,
        amountAdapter = amount,
    )
