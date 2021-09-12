@file:UseSerializers(Fruit.SealedifiedSerializer::class)

package pl.brightinventions.sealedified.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject
import pl.brightinventions.sealedified.Sealedified

/**
 * Note: something like this would not compile
 *
 * ```
 * @Serializable
 * data class FruitBasket(
 *     @Serializable(with = FruitsSerializer::class)
 *     val fruits: List<Sealedified<Fruit, JsonObject>>
 * ) {
 *     object FruitsSerializer : KSerializer<List<Sealedified<Fruit, JsonObject>>>
 *         by ListSerializer(Fruit.SealedifiedSerializer)
 * }
 * ```
 *
 * due to an error saying:
 *
 * > Serializer has not been found for type 'Sealedified<Fruit, JsonObject>'. To use context serializer as fallback,
 * > explicitly annotate type or property with @Contextual
 *
 * Therefore, the class is extracted to a separate file that uses [@file:UseSerializers][UseSerializers]
 *
 */
@Serializable
data class FruitBasket(
    val fruits: List<Sealedified<Fruit, JsonObject>>
)
