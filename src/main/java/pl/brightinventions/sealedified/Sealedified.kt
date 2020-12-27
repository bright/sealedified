package pl.brightinventions.sealedified

import kotlinx.serialization.json.JsonObject

sealed class Sealedified<T : Any> {
    data class Known<T : Any>(val value: T) : Sealedified<T>()
    data class Unknown<T : Any>(val jsonObject: JsonObject) : Sealedified<T>()

    fun knownOrNull() = (this as? Known<T>)?.value
}
