@file:Suppress("MatchingDeclarationName")

package dev.jonpoulton.actual.api.model.internal

import alakazam.kotlin.serialization.enumStringSerializer
import dev.jonpoulton.actual.api.model.ResponseStatus
import kotlinx.serialization.KSerializer

internal object ResponseStatusSerializer : KSerializer<ResponseStatus> by enumStringSerializer()
