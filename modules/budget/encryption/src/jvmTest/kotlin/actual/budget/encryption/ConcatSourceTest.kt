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
package actual.budget.encryption

import assertk.assertThat
import assertk.assertions.isEqualTo
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.buffer
import kotlin.test.Test

class ConcatSourceTest {
  @Test
  fun a() {
    val a = buffer("abc123")
    val b = buffer("xyz789")
    val c = a + b

    assertThat(c.buffer().use { it.readUtf8() })
      .isEqualTo("abc123xyz789")
  }

  private fun buffer(data: String): Buffer = Buffer().also { it.write(data.encodeUtf8()) }
}
