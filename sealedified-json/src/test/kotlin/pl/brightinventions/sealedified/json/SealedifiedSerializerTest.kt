package pl.brightinventions.sealedified.json

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pl.brightinventions.sealedified.Sealedified
import pl.miensol.shouldko.shouldEqual

internal class SealedifiedSerializerTest {

    private val apple = Fruit.Apple(5)
    private val appleSealedified: Sealedified<Fruit, JsonObject> = Sealedified.Known(apple)
    private val appleSerialized =
        """
            {
                "type": "apple",
                "size": 5
            }
        """.trimIndent()

    private val orange = Fruit.Orange("John")
    private val orangeSealedified: Sealedified<Fruit, JsonObject> = Sealedified.Known(orange)

    private val unknownBanana = JsonObject(
        mapOf(
            "type" to JsonPrimitive("banana"),
            "length" to JsonPrimitive(10.0)
        )
    )
    private val unknownBananaSealedified: Sealedified<Fruit, JsonObject> = UnknownJson(unknownBanana)
    private val unknownBananaSerialized =
        """
            {
                "type": "banana",
                "length": 10.0
            }
        """.trimIndent()

    private val unknownPlum = JsonObject(
        mapOf(
            "type" to JsonPrimitive("plum"),
            "color" to JsonPrimitive("purple")
        )
    )
    private val unknownPlumSealedified: Sealedified<Fruit, JsonObject> = UnknownJson(unknownPlum)

    private val mixedFruitBasket = FruitBasket(
        listOf(
            appleSealedified,
            unknownBananaSealedified,
            orangeSealedified,
            unknownPlumSealedified
        )
    )
    private val mixedFruitBasketSerialized =
        """
            {
                "fruits": [
                    {
                        "type": "apple",
                        "size": 5
                    },
                    {
                        "type": "banana",
                        "length": 10.0
                    },
                    {
                        "type": "orange",
                        "owner": "John"
                    },
                    {
                        "type": "plum",
                        "color": "purple"
                    }
                ]
            }
        """.trimIndent().trim()

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
        with(json.decodeFromString(Fruit.SealedifiedSerializer, appleSerialized)) {
            shouldEqual(appleSealedified)
            when (this) {
                is Sealedified.Known -> when (val knownValue = value) {
                    is Fruit.Apple -> knownValue.size.shouldEqual(5)
                    is Fruit.Orange -> throw AssertionError("It's supposed to be an apple")
                }
                is Sealedified.Unknown -> throw AssertionError("It's supposed to be known")
            }
        }
    }

    @Test
    fun `unknown fruit should be serialized`() {
        json.encodeToString(Fruit.SealedifiedSerializer, unknownBananaSealedified).shouldEqual(unknownBananaSerialized)
    }

    @Test
    fun `unknown fruit should be deserialized`() {
        with(json.decodeFromString(Fruit.SealedifiedSerializer, unknownBananaSerialized)) {
            shouldEqual(unknownBananaSealedified)
            when (this) {
                is Sealedified.Known -> throw AssertionError("It's supposed to be unknown")
                is Sealedified.Unknown -> raw.shouldEqual(unknownBanana)
            }
        }
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
    fun `mixed basket should be serialized`() {
        json.encodeToString(FruitBasket.serializer(), mixedFruitBasket).shouldEqual(mixedFruitBasketSerialized)
    }

    @Test
    fun `mixed basket should be deserialized`() {
        with(json.decodeFromString(FruitBasket.serializer(), mixedFruitBasketSerialized)) {
            shouldEqual(mixedFruitBasket)
            with(fruits[0]) {
                shouldEqual(appleSealedified)
                when (this) {
                    is Sealedified.Known -> when (val knownValue = value) {
                        is Fruit.Apple -> knownValue.size.shouldEqual(5)
                        is Fruit.Orange -> throw AssertionError("It's supposed to be an apple")
                    }
                    is Sealedified.Unknown -> throw AssertionError("It's supposed to be known")
                }
            }
            with(fruits[1]) {
                shouldEqual(unknownBananaSealedified)
                when (this) {
                    is Sealedified.Known -> throw AssertionError("It's supposed to be unknown")
                    is Sealedified.Unknown -> raw.shouldEqual(unknownBanana)
                }
            }
            with(fruits[2]) {
                shouldEqual(orangeSealedified)
                when (this) {
                    is Sealedified.Known -> when (val knownValue = value) {
                        is Fruit.Apple -> throw AssertionError("It's supposed to be an orange")
                        is Fruit.Orange -> knownValue.owner.shouldEqual("John")
                    }
                    is Sealedified.Unknown -> throw AssertionError("It's supposed to be known")
                }
            }
            with(fruits[3]) {
                shouldEqual(unknownPlumSealedified)
                when (this) {
                    is Sealedified.Known -> throw AssertionError("It's supposed to be unknown")
                    is Sealedified.Unknown -> raw.shouldEqual(unknownPlum)
                }
            }
        }
    }

    @Test
    fun `apple should be known`() {
        appleSealedified.knownOrNull().shouldEqual(apple)
    }

    @Test
    fun `unknown fruit should be null`() {
        unknownBananaSealedified.knownOrNull().shouldEqual(null)
    }
}
