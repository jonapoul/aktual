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
import kotlin.test.Test

class PercentTest {
  @Test
  fun `To string with decimals`() {
    assertThat(12.344.percent.toString(decimalPlaces = 2)).isEqualTo("12.34%")
    assertThat(12.346.percent.toString(decimalPlaces = 2)).isEqualTo("12.35%")
    assertThat(123.4.percent.toString(decimalPlaces = 2)).isEqualTo("123.40%")
    assertThat(123.4.percent.toString()).isEqualTo("123%")
  }
}
