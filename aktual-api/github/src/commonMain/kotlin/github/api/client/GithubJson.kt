package github.api.client

import kotlinx.serialization.json.Json

val GithubJson = Json {
  ignoreUnknownKeys = true
}
