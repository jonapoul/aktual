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
package aktual.test

import app.cash.turbine.TurbineTestContext
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo

suspend fun <T> TurbineTestContext<T>.assertThatNextEmission() =
  assertThat(awaitItem())

suspend fun <T> TurbineTestContext<T>.assertThatNextEmissionIsEqualTo(expected: T) =
  assertThatNextEmission().isEqualTo(expected)

suspend fun <T : Collection<*>> TurbineTestContext<T>.assertEmissionSize(expected: Int) =
  assertThat(awaitItem()).hasSize(expected)

suspend fun <T> TurbineTestContext<List<T>>.assertListEmitted(vararg expected: T) {
  val expectedList = expected.toList()

  val actualList = try {
    awaitItem()
  } catch (t: Throwable) {
    throw AssertionError("Timed out waiting for $expectedList", t)
  }

  assertThat(expectedList).isEqualTo(actualList)
}
