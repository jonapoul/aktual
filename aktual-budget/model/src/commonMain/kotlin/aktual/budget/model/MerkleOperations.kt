/** WARNING: SLOPIFIED CODE AHEAD */
@file:Suppress("MagicNumber", "LoopWithTooManyJumpStatements")

package aktual.budget.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Merkle trie operations for CRDT sync consistency checking. Implements the same algorithm as
 * packages/crdt/src/crdt/merkle.ts in the upstream Actual project. The trie is a base-3 radix trie
 * keyed on minutes-since-epoch, with XOR hashes at each node.
 */
object MerkleOperations {
  private const val KEY_LENGTH = 16
  private const val HASH_KEY = "hash"
  private val CHILD_KEYS = setOf("0", "1", "2")

  fun emptyTrie(): JsonObject = JsonObject(mapOf(HASH_KEY to JsonPrimitive(0)))

  /** Insert a timestamp into the trie, returning a new trie with updated hashes. */
  fun insert(trie: JsonObject, timestamp: Timestamp): JsonObject {
    val hash = murmurhash3(timestamp.toString())
    val key = (timestamp.milliseconds / 1000 / 60).toString(radix = 3)
    val updatedHash = trie.hash() xor hash
    val base = trie.toMutableMap()
    base[HASH_KEY] = JsonPrimitive(updatedHash)
    return insertKey(JsonObject(base), key, hash)
  }

  /**
   * Find the earliest divergence point between two tries. Returns milliseconds since epoch, or null
   * if in sync.
   */
  fun diff(trie1: JsonObject, trie2: JsonObject): Long? {
    if (trie1.hash() == trie2.hash()) return null

    var node1 = trie1
    var node2 = trie2
    var k = ""

    while (true) {
      val keys = (node1.childKeys() + node2.childKeys()).sorted()

      var diffKey: String? = null
      for (key in keys) {
        val next1 = node1.child(key)
        val next2 = node2.child(key)
        if (next1 == null || next2 == null) break
        if (next1.hash() != next2.hash()) {
          diffKey = key
          break
        }
      }

      if (diffKey == null) return keyToTimestamp(k)

      k += diffKey
      node1 = node1.child(diffKey) ?: emptyTrie()
      node2 = node2.child(diffKey) ?: emptyTrie()
    }
  }

  /** Prune the trie to keep only the last [n] children at each level. */
  fun prune(trie: JsonObject, n: Int = 2): JsonObject {
    if (trie.hash() == 0) return trie
    val keys = trie.childKeys().sorted()
    val result = mutableMapOf<String, JsonElement>(HASH_KEY to JsonPrimitive(trie.hash()))
    for (key in keys.takeLast(n)) {
      val child = trie.child(key) ?: continue
      result[key] = prune(child, n)
    }
    return JsonObject(result)
  }

  /** Convert a trie key back to a timestamp in milliseconds. */
  fun keyToTimestamp(key: String): Long {
    val fullKey = key + "0".repeat(KEY_LENGTH - key.length)
    return fullKey.toLong(radix = 3) * 1000L * 60L
  }

  private fun insertKey(trie: JsonObject, key: String, hash: Int): JsonObject {
    if (key.isEmpty()) return trie
    val c = key[0].toString()
    val existing = trie.child(c)
    val childNode = existing ?: JsonObject(emptyMap())
    val inserted = insertKey(childNode, key.substring(1), hash)
    val updatedChild = inserted.toMutableMap()
    updatedChild[HASH_KEY] = JsonPrimitive(childNode.hash() xor hash)
    val result = trie.toMutableMap()
    result[c] = JsonObject(updatedChild)
    return JsonObject(result)
  }

  private fun JsonObject.hash(): Int = this[HASH_KEY]?.jsonPrimitive?.int ?: 0

  private fun JsonObject.child(key: String): JsonObject? = this[key]?.jsonObject

  private fun JsonObject.childKeys(): List<String> = keys.filter { it in CHILD_KEYS }

  /**
   * MurmurHash3 (32-bit) implementation compatible with the murmurhash npm package's v3 function.
   */
  @Suppress("UnnecessaryParentheses")
  internal fun murmurhash3(input: String, seed: Int = 0): Int {
    val data = input.encodeToByteArray()
    val len = data.size
    val nblocks = len / 4
    var h1 = seed

    val c1 = 0xcc9e2d51.toInt()
    val c2 = 0x1b873593

    // Body
    for (i in 0 until nblocks) {
      val offset = i * 4
      var k1 =
        (data[offset].toInt() and 0xFF) or
          ((data[offset + 1].toInt() and 0xFF) shl 8) or
          ((data[offset + 2].toInt() and 0xFF) shl 16) or
          ((data[offset + 3].toInt() and 0xFF) shl 24)

      k1 *= c1
      k1 = k1.rotateLeft(15)
      k1 *= c2

      h1 = h1 xor k1
      h1 = h1.rotateLeft(13)
      h1 = h1 * 5 + 0xe6546b64.toInt()
    }

    // Tail
    val tail = nblocks * 4
    var k1 = 0
    when (len and 3) {
      3 -> {
        k1 = k1 xor ((data[tail + 2].toInt() and 0xFF) shl 16)
        k1 = k1 xor ((data[tail + 1].toInt() and 0xFF) shl 8)
        k1 = k1 xor (data[tail].toInt() and 0xFF)
        k1 *= c1
        k1 = k1.rotateLeft(15)
        k1 *= c2
        h1 = h1 xor k1
      }
      2 -> {
        k1 = k1 xor ((data[tail + 1].toInt() and 0xFF) shl 8)
        k1 = k1 xor (data[tail].toInt() and 0xFF)
        k1 *= c1
        k1 = k1.rotateLeft(15)
        k1 *= c2
        h1 = h1 xor k1
      }
      1 -> {
        k1 = k1 xor (data[tail].toInt() and 0xFF)
        k1 *= c1
        k1 = k1.rotateLeft(15)
        k1 *= c2
        h1 = h1 xor k1
      }
    }

    // Finalization
    h1 = h1 xor len
    h1 = h1 xor (h1 ushr 16)
    h1 *= 0x85ebca6b.toInt()
    h1 = h1 xor (h1 ushr 13)
    h1 *= 0xc2b2ae35.toInt()
    h1 = h1 xor (h1 ushr 16)

    return h1
  }
}
