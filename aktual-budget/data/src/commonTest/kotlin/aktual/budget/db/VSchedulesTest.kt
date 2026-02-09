package aktual.budget.db

import aktual.budget.db.test.insertPayeeMapping
import aktual.budget.db.test.insertRule
import aktual.budget.db.test.insertSchedule
import aktual.budget.db.test.insertScheduleJsonPaths
import aktual.budget.db.test.insertScheduleNextDate
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import org.intellij.lang.annotations.Language

internal class VSchedulesTest {
  @Test
  fun `Getting data from v_schedules table`() = runDatabaseTest {
    // given
    val scheduleId1 = "a2aea0d7-00fb-4cf1-ab38-8aa7fb19ab54"
    val scheduleId2 = "5c333823-2640-4d27-85a3-e97fabfe21d7"
    val scheduleId3 = "a9896b6c-a6f1-45b6-8384-61fc1f68fe62"
    insertScheduleJsonPaths(scheduleId1, payee = 0, account = 1, amount = 3, date = 2)
    insertScheduleJsonPaths(scheduleId2, payee = 0, account = 1, amount = 3, date = 2)
    insertScheduleJsonPaths(scheduleId3, payee = 0, account = 1, amount = 3, date = 2)

    val nextDateId1 = "d942b04a-8dba-4258-a83a-4e3c0c0193ef"
    val nextDateId2 = "1e1fb1fd-6784-4e83-ab91-a16629f3a6c5"
    val nextDateId3 = "6a88ae43-0a87-4ad5-a172-f86a5cee3ba5"
    insertScheduleNextDate(
        nextDateId1,
        scheduleId1,
        "2025-03-04",
        1728210343464,
        "2024-10-08",
        1728210343464,
    )
    insertScheduleNextDate(
        nextDateId2,
        scheduleId2,
        "2026-02-20",
        1705230381831,
        "2024-02-20",
        1705230381831,
    )
    insertScheduleNextDate(
        nextDateId3,
        scheduleId3,
        "2025-03-03",
        1705229050908,
        "2024-02-01",
        1705229050908,
    )

    val ruleId1 = "6e02242a-ebe0-4c7b-83e2-50a0501ded39"
    val ruleId2 = "a73fec91-c13b-4cca-a58d-0cfd7d6f7732"
    val ruleId3 = "4aee2c9f-3da1-4f1a-a2d6-0847100fdd20"
    insertRule(id = ruleId1, conditions = RULE_1_CONDITIONS, actions = RULE_1_ACTIONS)
    insertRule(id = ruleId2, conditions = RULE_2_CONDITIONS, actions = RULE_2_ACTIONS)
    insertRule(id = ruleId3, conditions = RULE_3_CONDITIONS, actions = RULE_3_ACTIONS)

    val payeeId1 = "2745db0b-b454-4877-a728-3c9f2b7056e5"
    val payeeId2 = "2002c385-1504-48bc-824f-be6b79e0e9ef"
    val payeeId3 = "866bd20a-dc48-4d2c-8d69-b9567a94fc32"
    insertPayeeMapping(payeeId1)
    insertPayeeMapping(payeeId2)
    insertPayeeMapping(payeeId3)

    insertSchedule(
        scheduleId1,
        ruleId1,
        completed = false,
        postsTransaction = true,
        tombstone = false,
        name = "A",
    )
    insertSchedule(
        scheduleId2,
        ruleId2,
        completed = false,
        postsTransaction = false,
        tombstone = false,
        name = "B",
    )
    insertSchedule(
        scheduleId3,
        ruleId3,
        completed = false,
        postsTransaction = false,
        tombstone = false,
        name = "C",
    )

    // then
    val expected1 =
        V_schedules(
            id = ScheduleId(scheduleId1),
            name = "A",
            rule = RuleId(ruleId1),
            next_date = LocalDate.parse("2025-03-04"),
            completed = false,
            posts_transaction = true,
            tombstone = false,
            _payee = PayeeId(payeeId1),
            _account = "78055dbe-680f-4605-bcd5-46a67feedcec",
            _amount = "0",
            _amountOp = "isapprox",
            _date =
                "{\"start\":\"2024-01-09\",\"interval\":1,\"frequency\":\"weekly\",\"patterns\":[]," +
                    "\"skipWeekend\":false,\"weekendSolveMode\":\"after\",\"endMode\":\"never\",\"endOccurrences\":1," +
                    "\"endDate\":\"2024-01-14\"}",
            _conditions = Json.parseToJsonElement(RULE_1_CONDITIONS).jsonArray,
            _actions = Json.parseToJsonElement(RULE_1_ACTIONS).jsonArray,
        )
    val expected2 =
        V_schedules(
            id = ScheduleId(scheduleId2),
            name = "B",
            rule = RuleId(ruleId2),
            next_date = LocalDate.parse("2026-02-20"),
            completed = false,
            posts_transaction = false,
            tombstone = false,
            _payee = PayeeId(payeeId2),
            _account = "eb08ea4f-bbb0-437f-873a-1fdee4154683",
            _amount = "-864",
            _amountOp = "isapprox",
            _date =
                "{\"start\":\"2023-02-20\",\"frequency\":\"yearly\",\"patterns\":[],\"skipWeekend\":false," +
                    "\"weekendSolveMode\":\"after\",\"endMode\":\"never\",\"endOccurrences\":1,\"endDate\":\"2024-01-14\"," +
                    "\"interval\":1}",
            _conditions = Json.parseToJsonElement(RULE_2_CONDITIONS).jsonArray,
            _actions = Json.parseToJsonElement(RULE_2_ACTIONS).jsonArray,
        )
    val expected3 =
        V_schedules(
            id = ScheduleId(scheduleId3),
            name = "C",
            rule = RuleId(ruleId3),
            next_date = LocalDate.parse("2025-03-03"),
            completed = false,
            posts_transaction = false,
            tombstone = false,
            _payee = PayeeId(payeeId3),
            _account = "78055dbe-680f-4605-bcd5-46a67feedcec",
            _amount = "-20000",
            _amountOp = "is",
            _date =
                "{\"start\":\"2024-02-01\",\"frequency\":\"monthly\",\"patterns\":[],\"skipWeekend\":true," +
                    "\"weekendSolveMode\":\"after\",\"endMode\":\"never\",\"endOccurrences\":1,\"endDate\":\"2024-01-14\"," +
                    "\"interval\":1}",
            _conditions = Json.parseToJsonElement(RULE_3_CONDITIONS).jsonArray,
            _actions = Json.parseToJsonElement(RULE_3_ACTIONS).jsonArray,
        )

    assertThat(schedulesQueries.getFromVSchedules().executeAsList())
        .isEqualTo(listOf(expected1, expected2, expected3))
  }
}

@Language("JSON")
private const val RULE_1_CONDITIONS =
    """
[
  {
    "op": "is",
    "field": "description",
    "value": "2745db0b-b454-4877-a728-3c9f2b7056e5",
    "type": "id"
  },
  {
    "op": "is",
    "field": "acct",
    "value": "78055dbe-680f-4605-bcd5-46a67feedcec",
    "type": "id"
  },
  {
    "op": "isapprox",
    "field": "date",
    "value": {
      "start": "2024-01-09",
      "interval": 1,
      "frequency": "weekly",
      "patterns": [],
      "skipWeekend": false,
      "weekendSolveMode": "after",
      "endMode": "never",
      "endOccurrences": 1,
      "endDate": "2024-01-14"
    },
    "type": "date"
  },
  {
    "op": "isapprox",
    "field": "amount",
    "value": 0
  }
]
"""

@Language("JSON")
private const val RULE_1_ACTIONS =
    """
[
  {
    "op": "link-schedule",
    "field": null,
    "value": "a2aea0d7-00fb-4cf1-ab38-8aa7fb19ab54",
    "type": "id"
  }
]
"""

@Language("JSON")
private const val RULE_2_CONDITIONS =
    """
[
  {
    "op": "is",
    "field": "description",
    "value": "2002c385-1504-48bc-824f-be6b79e0e9ef"
  },
  {
    "op": "is",
    "field": "acct",
    "value": "eb08ea4f-bbb0-437f-873a-1fdee4154683"
  },
  {
    "op": "isapprox",
    "field": "date",
    "value": {
      "start": "2023-02-20",
      "frequency": "yearly",
      "patterns": [],
      "skipWeekend": false,
      "weekendSolveMode": "after",
      "endMode": "never",
      "endOccurrences": 1,
      "endDate": "2024-01-14",
      "interval": 1
    }
  },
  {
    "op": "isapprox",
    "field": "amount",
    "value": -864
  }
]
"""

@Language("JSON")
private const val RULE_2_ACTIONS =
    """
[
  {
    "op": "link-schedule",
    "value": "5c333823-2640-4d27-85a3-e97fabfe21d7"
  }
]
"""

@Language("JSON")
private const val RULE_3_CONDITIONS =
    """
[
  {
    "op": "is",
    "field": "description",
    "value": "866bd20a-dc48-4d2c-8d69-b9567a94fc32"
  },
  {
    "op": "is",
    "field": "acct",
    "value": "78055dbe-680f-4605-bcd5-46a67feedcec"
  },
  {
    "op": "isapprox",
    "field": "date",
    "value": {
      "start": "2024-02-01",
      "frequency": "monthly",
      "patterns": [],
      "skipWeekend": true,
      "weekendSolveMode": "after",
      "endMode": "never",
      "endOccurrences": 1,
      "endDate": "2024-01-14",
      "interval": 1
    }
  },
  {
    "op": "is",
    "field": "amount",
    "value": -20000
  }
]
"""

@Language("JSON")
private const val RULE_3_ACTIONS =
    """
[
  {
    "op": "link-schedule",
    "value": "a9896b6c-a6f1-45b6-8384-61fc1f68fe62"
  }
]
"""
