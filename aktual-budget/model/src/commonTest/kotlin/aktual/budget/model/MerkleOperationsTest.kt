package aktual.budget.model

import aktual.budget.model.MerkleOperations.diff
import aktual.budget.model.MerkleOperations.emptyTrie
import aktual.budget.model.MerkleOperations.insert
import aktual.budget.model.MerkleOperations.keyToTimestamp
import aktual.budget.model.MerkleOperations.murmurhash3
import aktual.budget.model.MerkleOperations.prune
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Instant

class MerkleOperationsTest {
  @Test
  fun `empty trie has zero hash`() {
    val trie = emptyTrie()
    assertThat(trie["hash"]?.toString()).isEqualTo("0")
  }

  @Test
  fun `insert single timestamp`() {
    val trie = emptyTrie()
    val ts = timestamp("2024-03-16T18:14:09.237Z", counter = 0, node = "889aaebae6298282")
    val result = insert(trie, ts)
    assertThat(result["hash"]?.toString()).isNotNull()
  }

  @Test
  fun `diff of identical tries is null`() {
    val trie = emptyTrie()
    val ts = timestamp("2024-03-16T18:14:09.237Z", counter = 0, node = "889aaebae6298282")
    val t1 = insert(trie, ts)
    val t2 = insert(trie, ts)
    assertThat(diff(t1, t2)).isNull()
  }

  @Test
  fun `diff of empty tries is null`() {
    assertThat(diff(emptyTrie(), emptyTrie())).isNull()
  }

  @Test
  fun `diff of different tries returns a timestamp`() {
    val trie = emptyTrie()
    val ts1 = timestamp("2024-03-16T18:14:09.237Z", counter = 0, node = "aaaaaaaaaaaaaaaa")
    val ts2 = timestamp("2024-03-16T19:30:00.000Z", counter = 0, node = "bbbbbbbbbbbbbbbb")
    val t1 = insert(trie, ts1)
    val t2 = insert(insert(trie, ts1), ts2)
    val diffTime = diff(t1, t2)
    assertThat(diffTime).isNotNull()
  }

  @Test
  fun `insert and diff roundtrip - synced after applying same messages`() {
    val trie = emptyTrie()
    val timestamps =
      listOf(
        timestamp("2024-03-16T18:14:09.237Z", counter = 0, node = "aaaaaaaaaaaaaaaa"),
        timestamp("2024-03-16T18:15:00.000Z", counter = 1, node = "aaaaaaaaaaaaaaaa"),
        timestamp("2024-03-16T18:16:30.000Z", counter = 0, node = "bbbbbbbbbbbbbbbb"),
      )
    var t1 = trie
    var t2 = trie
    for (ts in timestamps) {
      t1 = insert(t1, ts)
      t2 = insert(t2, ts)
    }
    assertThat(diff(t1, t2)).isNull()
  }

  @Test
  fun `prune keeps last n children at each level`() {
    val trie = emptyTrie()
    val timestamps =
      listOf(
        timestamp("2024-01-01T00:00:00.000Z", counter = 0, node = "aaaaaaaaaaaaaaaa"),
        timestamp("2024-06-15T12:00:00.000Z", counter = 0, node = "aaaaaaaaaaaaaaaa"),
        timestamp("2024-12-31T23:59:00.000Z", counter = 0, node = "aaaaaaaaaaaaaaaa"),
      )
    var result = trie
    for (ts in timestamps) {
      result = insert(result, ts)
    }
    val pruned = prune(result)
    assertThat(diff(result, pruned)).isNull()
  }

  @Test
  fun `keyToTimestamp and back`() {
    val ts = timestamp("2024-03-16T18:14:00.000Z", counter = 0, node = "0000000000000000")
    val key = (ts.milliseconds / 1000 / 60).toString(radix = 3)
    val millis = keyToTimestamp(key)
    // Should be within 1 minute (60000ms) since we lose sub-minute precision
    val expectedMinutes = ts.milliseconds / 1000 / 60
    val actualMinutes = millis / 1000 / 60
    assertThat(actualMinutes).isEqualTo(expectedMinutes)
  }

  @Test
  fun `murmurhash3 matches npm murmurhash v3`() {
    // Verified against murmurhash npm package v2.0.1, murmurhash.v3("hello") => 613153351
    assertThat(murmurhash3("hello")).isEqualTo(613153351)
    // murmurhash.v3("") => 0
    assertThat(murmurhash3("")).isEqualTo(0)
    // murmurhash.v3("test") => 3127628307 (as signed: -1167339989 -- but we compare unsigned)
    assertThat(murmurhash3("test").toUInt()).isEqualTo(3127628307u)
  }

  private fun timestamp(instant: String, counter: Long, node: String) =
    Timestamp(instant = Instant.parse(instant), counter = counter, node = node)
}
