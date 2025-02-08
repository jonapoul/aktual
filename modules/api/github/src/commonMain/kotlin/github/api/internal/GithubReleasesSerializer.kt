package github.api.internal

import github.api.model.GithubRelease
import github.api.model.GithubReleases
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal object GithubReleasesSerializer : KSerializer<GithubReleases> {
  override val descriptor = buildSerialDescriptor(serialName = "GithubReleases", kind = PolymorphicKind.SEALED)

  override fun serialize(encoder: Encoder, value: GithubReleases) = error("Serialization is unsupported for $value")

  override fun deserialize(decoder: Decoder): GithubReleases {
    val input = decoder as? JsonDecoder ?: error("Invalid decoder $decoder")
    val json = input.json
    val rootElement = input.decodeJsonElement()

    return if (rootElement is JsonArray) {
      val serializer = ListSerializer(GithubRelease.serializer())
      val list = json.decodeFromJsonElement(serializer, rootElement)
      GithubReleases.Valid(list)
    } else {
      json.decodeFromJsonElement(GithubReleases.Invalid.serializer(), rootElement)
    }
  }
}
