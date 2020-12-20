@file:UseSerializers(SealedifiedSerializer::class)

package pl.brightinventions.sealedified

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pl.miensol.shouldko.shouldEqual

internal class SealedifiedSerializerTest {

    @Serializable
    private sealed class Fruit {

        @Serializable
        @SerialName("apple")
        data class Apple(val size: Int) : Fruit()

        @Serializable
        @SerialName("orange")
        data class Orange(val owner: String?) : Fruit()

        @Serializable
        @SerialName("tree")
        data class Tree(
            val age: Int,
            val fruits: List<Sealedified<Fruit>>
        ) : Fruit()

        object SealedifiedSerializer : KSerializer<Sealedified<Fruit>> by sealedifiedSerializer(serializer())
    }

    @Serializable
    private data class FruitWrapper(val fruit: Sealedified<Fruit>?)

    private val apple = Fruit.Apple(5)
    private val appleSealedified: Sealedified<Fruit> = Sealedified.Known(apple)
    private val appleSerialized =
        """
            {
                "type": "apple",
                "size": 5
            }
        """.trimIndent()

    private val unknownFruit = Sealedified.Unknown<Fruit>(
        JsonObject(
            mapOf(
                "type" to JsonPrimitive("banana"),
                "length" to JsonPrimitive(10.0)
            )
        )
    )
    private val unknownFruitSerialized =
        """
            {
                "type": "banana",
                "length": 10.0
            }
        """.trimIndent()

    private val tree: Sealedified<Fruit> = Sealedified.Known(
        Fruit.Tree(
            age = 100,
            fruits = listOf(
                Sealedified.Known(Fruit.Apple(1)),
                Sealedified.Known(Fruit.Orange("John")),
                Sealedified.Known(
                    Fruit.Tree(
                        age = 50,
                        fruits = listOf(
                            Sealedified.Known(Fruit.Orange(null)),
                            unknownFruit,
                            Sealedified.Known(Fruit.Apple(2))
                        )
                    )
                )
            )
        )
    )

    private val treeSerialized =
        """
            {
                "type": "tree",
                "age": 100,
                "fruits": [
                    {
                        "type": "apple",
                        "size": 1
                    },
                    {
                        "type": "orange",
                        "owner": "John"
                    },
                    {
                        "type": "tree",
                        "age": 50,
                        "fruits": [
                            {
                                "type": "orange",
                                "owner": null
                            },
                            {
                                "type": "banana",
                                "length": 10.0
                            },
                            {
                                "type": "apple",
                                "size": 2
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

    private val incorrectJsonTypeSerialized =
        """
            [
                "this array should not be deserialized"
            ]
        """.trimIndent()

    private val wrapperNull = FruitWrapper(null)
    private val wrapperNullSerialized = """
        {
            "fruit": null
        }
    """.trimIndent()

    private val json = Json {
        prettyPrint = true
    }

    @Test
    fun `apple should be serialized`() {
        json.encodeToString(Fruit.SealedifiedSerializer, appleSealedified).shouldEqual(appleSerialized)
    }

    @Test
    fun `apple should be deserialized`() {
        json.decodeFromString(Fruit.SealedifiedSerializer, appleSerialized).shouldEqual(appleSealedified)
    }

    @Test
    fun `unknown fruit should be serialized`() {
        json.encodeToString(Fruit.SealedifiedSerializer, unknownFruit).shouldEqual(unknownFruitSerialized)
    }

    @Test
    fun `unknown fruit should be deserialized`() {
        json.decodeFromString(Fruit.SealedifiedSerializer, unknownFruitSerialized).shouldEqual(unknownFruit)
    }

    @Test
    fun `tree should be serialized`() {
        json.encodeToString(Fruit.SealedifiedSerializer, tree).shouldEqual(treeSerialized)
    }

    @Test
    fun `tree should be deserialized`() {
        json.decodeFromString(Fruit.SealedifiedSerializer, treeSerialized).shouldEqual(tree)
    }

    @Test
    fun `JSON array should not be deserialized`() {
        Assertions.assertThrows(SerializationException::class.java) {
            json.decodeFromString(Fruit.SealedifiedSerializer, incorrectJsonTypeSerialized)
        }
    }

    @Test
    fun `wrapper with null should be serialized`() {
        json.encodeToString(FruitWrapper.serializer(), wrapperNull).shouldEqual(wrapperNullSerialized)
    }

    @Test
    fun `wrapper with null should be deserialized`() {
        json.decodeFromString(FruitWrapper.serializer(), wrapperNullSerialized).shouldEqual(wrapperNull)
    }

    @Test
    fun `apple should be known`() {
        appleSealedified.knownOrNull().shouldEqual(apple)
    }

    @Test
    fun `unknown fruit should be null`() {
        unknownFruit.knownOrNull().shouldEqual(null)
    }
}
