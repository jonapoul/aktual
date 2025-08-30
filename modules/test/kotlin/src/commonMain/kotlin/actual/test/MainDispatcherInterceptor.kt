package actual.test

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherInterceptor(
  val main: CoroutineDispatcher = UnconfinedTestDispatcher(),
) : TestInterceptor {
  override fun intercept(testFunction: TestFunction) {
    try {
      Dispatchers.setMain(main)
      testFunction()
    } finally {
      Dispatchers.resetMain()
    }
  }
}
