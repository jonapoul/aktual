package actual.logging

import alakazam.kotlin.logging.Logger
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AndroidTreeFactoryTest {
  private lateinit var context: Context

  @BeforeTest
  fun before() {
    Logger.uprootAll()
    context = ApplicationProvider.getApplicationContext()
  }

  @AfterTest
  fun after() {
    Logger.uprootAll()
  }

  // THIS TEST WILL FAIL IF WE ADD ANY TESTS ABOVE, OR IF WE ADD ANY NEW IMPORTS
  @Test
  fun `Log to logcat`() {
    // given
    val factory = AndroidTreeFactory
    ActualLogging.init(factory, AndroidLogStorage(context))

    // when
    Logger.i("Hello world")

    // then
    assertEquals(
      actual = ShadowLog.getLogs().last(),
      expected = ShadowLog.LogItem(
        Log.INFO,
        "AndroidTreeFactoryTest.kt:39", // CHANGE THIS NUMBER IF WE EDIT THIS TEST
        "ACTUAL: Hello world",
        null,
      ),
    )
  }

  @Test
  fun `Log to file asynchronously`() {
    // given
    val factory = AndroidTreeFactory
    ActualLogging.init(factory, AndroidLogStorage(context))

    // when
    Logger.i("Hello world")
    Logger.v("This is ignored because it's verbose")
    Logger.d("This is just on the edge")
    Logger.e("Here's an error, with a formatted argument: %04d", 123)

    // then
    val logDir = context.filesDir.resolve("../logs")
    while (!logDir.exists()) {
      Thread.sleep(100L)
    }

    val lines = logDir
      .listFiles()
      .orEmpty()
      .first()
      .readLines()
    assertEquals(expected = 3, actual = lines.size)
    assertTrue(lines[0].endsWith("INFO  Hello world"))
    assertTrue(lines[1].endsWith("DEBUG This is just on the edge"))
    assertTrue(lines[2].endsWith("ERROR Here's an error, with a formatted argument: 0123"))
  }
}
