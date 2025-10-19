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
package aktual.budget.db

import aktual.budget.db.test.getMetaValue
import aktual.budget.db.test.insertMeta
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

internal class MetaQueriesTest {
  @Test
  fun `Getting from empty table returns null`() = runDatabaseTest {
    assertThat(getMetaValue(key = "test")).isNull()
  }

  @Test
  fun `Inserting then getting`() = runDatabaseTest {
    assertThat(getMetaValue(key = "myKey")).isNull()
    insertMeta(key = "myKey", value = "myValue")
    assertThat(getMetaValue(key = "myKey")).isEqualTo("myValue")
  }
}
