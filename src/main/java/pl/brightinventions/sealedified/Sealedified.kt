package pl.brightinventions.sealedified

import kotlinx.serialization.json.JsonObject

sealed class Sealedified<T> {
    class Known<T>(val value: T) : Sealedified<T>()
    class Unknown<T>(val jsonElement: JsonObject) : Sealedified<T>()
}
