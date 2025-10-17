package actual.prefs

import actual.test.assertThatNextEmissionIsEqualTo
import app.cash.turbine.test
import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.fail
import dev.jonpoulton.preferences.core.NullableStringSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.StringSerializer
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import java.util.prefs.Preferences as JPreferences

class JvmPreferencesTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  private lateinit var jPreferences: JPreferences
  private lateinit var preferences: JvmPreferences

  @Before
  fun before() {
    jPreferences = JPreferences.userNodeForPackage(JvmPreferencesTest::class.java)
    preferences = JvmPreferences(jPreferences)
  }

  @After
  fun after() {
    jPreferences.removeNode()
  }

  @Test
  fun `Get from empty prefs`() = with(preferences) {
    val string = getString("abc", "xyz")
    val bool = getBoolean("abc", false)
    val float = getFloat("abc", 7.89f)
    val int = getInt("abc", 123)
    val long = getLong("abc", 456L)

    assertThat(string).isNotSet()
    assertThat(bool).isNotSet()
    assertThat(float).isNotSet()
    assertThat(int).isNotSet()
    assertThat(long).isNotSet()

    assertThat(string.get()).isEqualTo("xyz")
    assertThat(bool.get()).isEqualTo(false)
    assertThat(float.get()).isEqualTo(7.89f)
    assertThat(int.get()).isEqualTo(123)
    assertThat(long.get()).isEqualTo(456L)
  }

  private fun <T> Assert<Preference<T>>.isNotSet() = given { pref ->
    if (!pref.isSet()) return@given
    fail(message = "Preference should be empty", actual = pref.get())
  }

  @Test
  fun `Observe string via flow`() = runTest {
    with(preferences.getString("abc", "xyz")) {
      asFlow().test {
        assertThatNextEmissionIsEqualTo("xyz")
        set("abc")
        assertThatNextEmissionIsEqualTo("abc")
        delete()
        assertThatNextEmissionIsEqualTo("xyz")
      }
    }
  }

  @Test
  fun `Observe int via flow when other keys are changing`() = runTest {
    val intPreference = preferences.getInt("abc", default = 123)
    intPreference.asFlow().test {
      // given
      assertThatNextEmissionIsEqualTo(123)

      preferences.getInt(key = "def", default = 1).setAndCommit(2)
      expectNoEvents()

      preferences.getFloat(key = "ghi", default = 1.23f).setAndCommit(2.34f)
      expectNoEvents()

      preferences.getBoolean(key = "jkl", default = false).setAndCommit(true)
      expectNoEvents()

      intPreference.setAndCommit(456)
      assertThatNextEmissionIsEqualTo(456)
    }
  }

  @Serializable
  private data class TestObject(val int: Int, val string: String)

  private object TestObjectSerializer : StringSerializer<TestObject> {
    override fun serialize(value: TestObject): String = Json.encodeToString(value)
    override fun deserialize(value: String): TestObject = Json.decodeFromString(value)
  }

  @Test
  fun `Serializable object`() = runTest {
    val default = TestObject(int = 123, string = "def")
    with(preferences.getObject("abc", TestObjectSerializer, default)) {
      asFlow().test {
        assertThatNextEmissionIsEqualTo(default)

        val value = TestObject(int = 789, string = "xyz")
        set(value)
        assertThatNextEmissionIsEqualTo(value)

        delete()
        assertThatNextEmissionIsEqualTo(default)
      }
    }
  }

  private object TestObjectNullableSerializer : NullableStringSerializer<TestObject> {
    override fun serialize(value: TestObject?): String? = value?.let(Json::encodeToString)
    override fun deserialize(value: String?): TestObject? = value?.let(Json::decodeFromString)
  }

  @Test
  fun `Serializable nullable object`() = runTest {
    with(preferences.getNullableObject("abc", TestObjectNullableSerializer, default = null)) {
      asFlow().test {
        assertThatNextEmissionIsEqualTo(null)

        val value = TestObject(int = 789, string = "xyz")
        set(value)
        assertThatNextEmissionIsEqualTo(value)

        delete()
        assertThatNextEmissionIsEqualTo(null)
      }
    }
  }
}
