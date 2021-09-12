package pl.brightinventions.sealedified.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import pl.brightinventions.sealedified.Sealedified

@Serializable
data class FruitWrapper(
    @Serializable(with = Fruit.SealedifiedSerializer::class)
    val fruit: Sealedified<Fruit, JsonObject>?
)