package pl.brightinventions.sealedified.json

import kotlinx.serialization.json.JsonObject
import pl.brightinventions.sealedified.Sealedified

data class UnknownJson<T : Any>(val raw: JsonObject) : Sealedified.Unknown<T, JsonObject>()
