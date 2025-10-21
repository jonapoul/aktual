/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
@file:OptIn(ExperimentalCoroutinesApi::class)

package aktual.test

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class MainDispatcherInterceptor(
  val main: CoroutineDispatcher = UnconfinedTestDispatcher(),
) : CoroutineTestInterceptor {
  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    try {
      Dispatchers.setMain(main)
      testFunction()
    } finally {
      Dispatchers.resetMain()
    }
  }
}
