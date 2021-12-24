package pl.brightinventions.sealedified.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import pl.brightinventions.sealedified.Sealedified

@Serializable
data class FruitBasket(
    val fruits: List<@Serializable(with = Fruit.SealedifiedSerializer::class) Sealedified<Fruit, JsonObject>>
)
