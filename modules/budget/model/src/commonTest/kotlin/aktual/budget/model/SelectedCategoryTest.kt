/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.model

import aktual.test.isEqualToList
import assertk.assertThat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test

class SelectedCategoryTest {
  @Test
  fun `Parse categories list`() {
    val serializer = ListSerializer(SelectedCategory.serializer())
    val decoded = Json.decodeFromString(serializer, JSON)
    assertThat(decoded).isEqualToList(ITEM_1, ITEM_2)
  }

  private companion object {
    private val JSON = """
      [
        {
          "cat_group": "7eded805-bc32-45fa-911c-441e6cc11ece",
          "goal_def": null,
          "hidden": 0,
          "id": "9099179f-9105-4679-94c9-7e4e6465ce34",
          "is_income": 0,
          "name": "Student Loan",
          "sort_order": 16384,
          "tombstone": 0
        },
        {
          "cat_group": "aefad869-eb9e-4794-b9be-2d8e6a8c4e3b",
          "goal_def": null,
          "hidden": 0,
          "id": "e49ecc6b-9ce5-4351-a07a-05f607e671d7",
          "is_income": 0,
          "name": "Interest",
          "sort_order": 8192,
          "tombstone": 0
        }
      ]
    """.trimIndent()

    private val ITEM_1 = SelectedCategory(
      id = CategoryId("9099179f-9105-4679-94c9-7e4e6465ce34"),
      name = "Student Loan",
      groupId = CategoryGroupId("7eded805-bc32-45fa-911c-441e6cc11ece"),
      isIncome = false,
      sortOrder = 16384.0,
      goalDef = null,
      isHidden = false,
      tombstone = false,
    )

    private val ITEM_2 = SelectedCategory(
      id = CategoryId("e49ecc6b-9ce5-4351-a07a-05f607e671d7"),
      name = "Interest",
      groupId = CategoryGroupId("aefad869-eb9e-4794-b9be-2d8e6a8c4e3b"),
      isIncome = false,
      sortOrder = 8192.0,
      goalDef = null,
      isHidden = false,
      tombstone = false,
    )
  }
}
