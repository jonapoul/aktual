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
package aktual.core.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.isLessThanOrEqualTo
import kotlin.test.Test

class BytesTest {
  @Test
  fun comparisons() {
    assertThat(1.bytes).isLessThan(2.bytes)
    assertThat(1.bytes).isLessThanOrEqualTo(2.bytes)
    assertThat(2.bytes).isLessThanOrEqualTo(2.bytes)
    assertThat(2.bytes).isGreaterThanOrEqualTo(2.bytes)
  }

  @Test
  fun conversions() {
    assertThat(1.MB).isEqualTo(1000.kB)
  }

  @Test
  fun convertToString() {
    assertThat(0.bytes.toString()).isEqualTo("0 B")
    assertThat(1234.bytes.toString()).isEqualTo("1.2 kB")
    assertThat(1234.bytes.toString(precision = 2)).isEqualTo("1.23 kB")
    assertThat(1234.bytes.toString(precision = 3)).isEqualTo("1.234 kB")
    assertThat(1234.bytes.toString(precision = 0)).isEqualTo("1 kB")

    assertThat(7.89.kB.toString(precision = 3)).isEqualTo("7.890 kB")
    assertThat(7.89.MB.toString(precision = 3)).isEqualTo("7.890 MB")
    assertThat(7.89.GB.toString(precision = 3)).isEqualTo("7.890 GB")
    assertThat(7.89.TB.toString(precision = 3)).isEqualTo("7.890 TB")
  }

  @Test
  fun multiply() {
    assertThat(8.bytes * 2).isEqualTo(16.bytes)
    assertThat(8.bytes * 1).isEqualTo(8.bytes)
  }
}
