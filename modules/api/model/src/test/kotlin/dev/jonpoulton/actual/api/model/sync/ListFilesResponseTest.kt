package dev.jonpoulton.actual.api.model.sync

import dev.jonpoulton.actual.api.json.ActualJson
import org.junit.Test
import kotlin.test.assertEquals

class ListFilesResponseTest {
  @Test
  fun `Deserialize ok`() {
    val json = """
      {
        "status": "ok",
        "data": [
          {
            "deleted": 0,
            "fileId": "b328186c-c919-4333-959b-04e676c1ee58",
            "groupId": "afb25fc0-a294-4f71-ae8f-ce1e3a8fed20",
            "name": "Main Budget",
            "encryptKeyId": "2a66f4de-c530-4c06-8103-a48e26a0ce67"
          },
          {
            "deleted": 1,
            "fileId": "667cfb1c-4901-4619-9b9f-f966dfd63156",
            "groupId": "421cef82-59d2-401b-a702-b098178dd912",
            "name": "My Finances",
            "encryptKeyId": null
          }
        ]
      }
    """.trimIndent()

    assertEquals(
      actual = ActualJson.decodeFromString<ListFilesResponse>(json),
      expected = ListFilesResponse(
        data = listOf(
          ListFilesResponse.Data(
            name = "Main Budget",
            fileId = "b328186c-c919-4333-959b-04e676c1ee58",
            groupId = "afb25fc0-a294-4f71-ae8f-ce1e3a8fed20",
            encryptKeyId = "2a66f4de-c530-4c06-8103-a48e26a0ce67",
            deleted = false,
          ),
          ListFilesResponse.Data(
            name = "My Finances",
            fileId = "667cfb1c-4901-4619-9b9f-f966dfd63156",
            groupId = "421cef82-59d2-401b-a702-b098178dd912",
            encryptKeyId = null,
            deleted = true,
          ),
        ),
      ),
    )
  }
}
