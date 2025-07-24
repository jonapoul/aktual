package actual.api.client

import org.intellij.lang.annotations.Language

@Language("JSON")
internal val HEALTH_SUCCESS = """
{
    "status": "UP"
}
""".trimIndent()
