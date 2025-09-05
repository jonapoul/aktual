package actual.test

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherInterceptor(
  val main: CoroutineDispatcher = UnconfinedTestDispatcher(),
) : TestInterceptor, CoroutineTestInterceptor {
  override suspend fun intercept(testFunction: CoroutineTestFunction) = runTest { testFunction() }

  override fun intercept(testFunction: TestFunction) = runTest { testFunction() }

  private inline fun runTest(testFunction: () -> Unit) = try {
    Dispatchers.setMain(main)
    testFunction()
  } finally {
    Dispatchers.resetMain()
  }
}
