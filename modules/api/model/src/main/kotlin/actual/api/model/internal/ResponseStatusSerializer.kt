package actual.api.model.internal

import actual.api.model.ResponseStatus
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer

internal object ResponseStatusSerializer : KSerializer<ResponseStatus> by enumStringSerializer()
