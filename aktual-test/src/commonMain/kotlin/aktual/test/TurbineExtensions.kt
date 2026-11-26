@file:Suppress("SuspendFunWithCoroutineScopeReceiver")

package aktual.test

import app.cash.turbine.TurbineTestContext
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo

suspend fun <T> TurbineTestContext<T>.assertThatNextEmission() = assertThat(awaitItem())

suspend fun <T> TurbineTestContext<T>.assertThatNextEmissionIsEqualTo(expected: T) =
  assertThatNextEmission().isEqualTo(expected)

suspend fun <T : Collection<*>> TurbineTestContext<T>.assertEmissionSize(expected: Int) =
  assertThat(awaitItem()).hasSize(expected)
