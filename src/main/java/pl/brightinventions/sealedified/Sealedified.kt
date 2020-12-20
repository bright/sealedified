package pl.brightinventions.sealedified

import kotlinx.serialization.json.JsonObject

sealed class Sealedified<T> {
    data class Known<T>(val value: T) : Sealedified<T>()
    data class Unknown<T>(val jsonObject: JsonObject) : Sealedified<T>()
}
