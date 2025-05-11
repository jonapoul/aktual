package actual.api.client

import org.intellij.lang.annotations.Language

@Language("JSON")
internal val BASE_FETCH_INFO_SUCCESS = """
{
    "build": {
        "name": "@actual-app/sync-server",
        "description": "actual syncing server",
        "version": "25.4.0"
    }
}
""".trimIndent()
