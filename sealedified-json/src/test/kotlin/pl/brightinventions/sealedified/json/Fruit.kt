package pl.brightinventions.sealedified.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import pl.brightinventions.sealedified.Sealedified

@Serializable
sealed class Fruit {

    @Serializable
    @SerialName("apple")
    data class Apple(val size: Int) : Fruit()

    @Serializable
    @SerialName("orange")
    data class Orange(val owner: String?) : Fruit()

    object SealedifiedSerializer :
        KSerializer<Sealedified<Fruit, JsonObject>> by SealedifiedJsonSerializer(serializer())
}