/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.core.model

import alakazam.kotlin.core.parse

data class ServerUrl(
  val protocol: Protocol,
  val baseUrl: String,
) {
  init {
    require(baseUrl.isNotBlank()) { "Base URL is blank" }
  }

  override fun toString(): String = "$protocol://$baseUrl"

  companion object {
    val Demo = ServerUrl(protocol = Protocol.Https, baseUrl = "demo.actualbudget.org")
  }
}

fun ServerUrl(string: String): ServerUrl {
  val split = string.split("://")
  require(split.size == 2) { "Need a URL in format 'PROTOCOL://BASE_URL', got $string" }
  val protocol = Protocol::class.parse(split[0])
  val baseUrl = split[1]
  return ServerUrl(protocol, baseUrl)
}
