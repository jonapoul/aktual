package aktual.crashes.vm

import aktual.core.logging.CrashReport
import aktual.core.model.CrashFile
import aktual.crashes.data.CrashesRepository
import aktual.prefs.AppPreferences
import aktual.prefs.AppPreferencesImpl
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import aktual.test.unconfinedViewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime

class CrashPromptViewModelTest {
  private lateinit var preferences: AppPreferences
  private lateinit var repository: CrashesRepository

  private fun TestScope.before(vararg reports: CrashReport) {
    preferences = AppPreferencesImpl(buildPreferences(unconfinedDispatcher))
    repository = mockk { coEvery { reports() } returns reports.toList() }
  }

  private fun TestScope.viewModel() =
    CrashPromptViewModel(repository, preferences, viewModelContext = unconfinedViewModelContext)

  @Test
  fun `No reports means no prompt`() = runTest {
    before()
    viewModel().unseen.test { assertThatNextEmissionIsEqualTo(null) }
  }

  @Test
  fun `Prompt for the newest unseen report`() = runTest {
    before(NEWER, OLDER)
    viewModel().unseen.test { assertThatNextEmissionIsEqualTo(NEWER) }
  }

  @Test
  fun `Marking seen hides the prompt and saves the file`() = runTest {
    before(NEWER, OLDER)
    val viewModel = viewModel()
    viewModel.unseen.test {
      assertThatNextEmissionIsEqualTo(NEWER)
      viewModel.markSeen()
      assertThatNextEmissionIsEqualTo(null)
    }
    assertThat(preferences.lastSeenCrash.get()).isEqualTo(NEWER.file)
  }

  @Test
  fun `No prompt once the newest report is seen`() = runTest {
    before(NEWER, OLDER)
    preferences.lastSeenCrash.set(NEWER.file)
    viewModel().unseen.test { assertThatNextEmissionIsEqualTo(null) }
  }

  @Test
  fun `Prompt again for a newer crash`() = runTest {
    before(NEWER, OLDER)
    preferences.lastSeenCrash.set(OLDER.file)
    viewModel().unseen.test { assertThatNextEmissionIsEqualTo(NEWER) }
  }

  private companion object {
    val OLDER = report("crash-20261017-120000-000.txt", day = 17)
    val NEWER = report("crash-20261018-120000-000.txt", day = 18)

    fun report(name: String, day: Int) =
      CrashReport(
        file = CrashFile(name),
        time = LocalDateTime(2026, 10, day, 12, 0),
        version = "1.0",
        platform = "Linux",
        thread = "main",
        stackTrace = "java.lang.Error: Boom",
      )
  }
}
