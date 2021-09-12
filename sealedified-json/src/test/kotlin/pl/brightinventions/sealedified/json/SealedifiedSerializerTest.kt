package pl.brightinventions.sealedified.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pl.brightinventions.sealedified.Sealedified
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

        object SealedifiedSerializer :
            KSerializer<Sealedified<Fruit, JsonObject>> by SealedifiedJsonSerializer(serializer())
    }

    @Serializable
    private data class FruitWrapper(
        @Serializable(with = Fruit.SealedifiedSerializer::class)
        val fruit: Sealedified<Fruit, JsonObject>?
    )

    private val apple = Fruit.Apple(5)
    private val appleSealedified: Sealedified<Fruit, JsonObject> = Sealedified.Known(apple)
    private val appleSerialized =
        """
            {
                "type": "apple",
                "size": 5
            }
        """.trimIndent()

    private val unknownFruit = UnknownJson<Fruit>(
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
