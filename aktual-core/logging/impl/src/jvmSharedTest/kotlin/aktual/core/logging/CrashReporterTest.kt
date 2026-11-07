package aktual.core.logging

import aktual.core.model.CrashFile
import aktual.test.TemporaryFolder
import alakazam.test.TestClock
import app.cash.burst.InterceptTest
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import assertk.assertions.startsWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.Path

class CrashReporterTest {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  private val fileSystem = FileSystem.SYSTEM
  private lateinit var storage: LogStorage
  private var now = NOW

  @BeforeTest
  fun before() {
    val dir = temporaryFolder.newFolder("logs")
    storage = LogStorage { dir }
  }

  @Test
  fun `Write a report for the throwable`() {
    val path = reporter().write("main", IllegalStateException("Boom"))

    assertThat(path.name).isEqualTo("crash-20260207-161522-572.txt")
    val report = CrashReportFormat.parse(CrashFile(path.name), fileSystem.read(path) { readUtf8() })
    assertThat(report).isNotNull().all {
      prop(CrashReport::version).isEqualTo("1.2.3 (abc)")
      prop(CrashReport::platform).isEqualTo("Test OS")
      prop(CrashReport::thread).isEqualTo("main")
      prop(CrashReport::summary).isEqualTo("java.lang.IllegalStateException: Boom")
      prop(CrashReport::exceptionName).isEqualTo("IllegalStateException")
      prop(CrashReport::stackTrace).startsWith("java.lang.IllegalStateException: Boom\n\tat ")
    }
  }

  @Test
  fun `Only report the same throwable once`() {
    val reporter = reporter()
    val thread = Thread.currentThread()
    val first = IllegalStateException("First")

    reporter.uncaughtException(thread, first)
    now += 1.minutes
    reporter.uncaughtException(thread, first)
    now += 1.minutes
    reporter.uncaughtException(thread, IllegalStateException("Second"))

    assertThat(names()).hasSize(2)
  }

  @Test
  fun `Prune reports past the max age`() {
    val reporter = reporter()
    write(reporter, NOW - 31.days)
    val recent = write(reporter, NOW - 29.days)
    val latest = write(reporter, NOW)

    reporter.prune()

    assertThat(names()).containsExactlyInAnyOrder(recent.name, latest.name)
  }

  @Test
  fun `Prune the oldest reports past the max count`() {
    val reporter = reporter()
    val paths = (0..CrashReporter.MAX_COUNT).map { i -> write(reporter, NOW - i.minutes) }

    reporter.prune()

    val kept = paths.take(CrashReporter.MAX_COUNT).map { it.name }
    assertThat(names().sorted()).isEqualTo(kept.sorted())
  }

  private fun reporter() =
    CrashReporter(
      storage = storage,
      version = "1.2.3 (abc)",
      platform = "Test OS",
      clock = TestClock { now },
      timeZone = { UTC },
      fileSystem = fileSystem,
    )

  private fun write(reporter: CrashReporter, time: Instant): Path {
    now = time
    return reporter.write("main", RuntimeException(time.toLocalDateTime(UTC).toString()))
  }

  private fun names(): List<String> = fileSystem.list(storage.crashDirectory()).map { it.name }

  private companion object {
    // Sat Feb 07 2026 16:15:22.572 UTC
    val NOW = Instant.fromEpochMilliseconds(1_770_480_922_572L)
  }
}
