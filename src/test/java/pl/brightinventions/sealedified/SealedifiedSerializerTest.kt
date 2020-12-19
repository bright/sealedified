package pl.brightinventions.sealedified

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
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
        data class Tree(val age: Int, val fruits: List<Fruit>) : Fruit()
    }

    private val regularFruitSerializer = Fruit.serializer()
    private val sealedifiedFruitSerializer = SealedifiedSerializer(regularFruitSerializer)

    private val apple = Fruit.Apple(5)
    private val appleSealedified: Sealedified<Fruit> = Sealedified.Known(apple)
    private val appleSerialized =
        """
            {
                "type": "apple",
                "size": 5
            }
        """.trimIndent()

    private val tree: Sealedified<Fruit> = Sealedified.Known(
        Fruit.Tree(
            age = 100,
            fruits = listOf(
                Fruit.Apple(1),
                Fruit.Orange("John"),
                Fruit.Tree(
                    age = 50,
                    fruits = listOf(
                        Fruit.Orange(null),
                        Fruit.Apple(2)
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
                                "type": "apple",
                                "size": 2
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

    private val jsonPrettyConfig = JsonConfiguration.Stable.copy(prettyPrint = true, useArrayPolymorphism = false)
    private val json = Json(jsonPrettyConfig)

    @Test
    fun `apple should be serialized`() {
        json.stringify(regularFruitSerializer, apple).shouldEqual(appleSerialized)
    }

    //@Test
    fun `apple should be deserialized`() {
        json.parse(regularFruitSerializer, appleSerialized).shouldEqual(apple)
    }

    @Test
    fun `apple sealedified should be serialized`() {
        json.stringify(sealedifiedFruitSerializer, appleSealedified).shouldEqual(appleSerialized)
    }

    //@Test
    fun `apple sealedified should be deserialized`() {
        json.parse(sealedifiedFruitSerializer, appleSerialized).shouldEqual(appleSealedified)
    }

    //@Test
    fun `tree should be serialized`() {
        json.stringify(sealedifiedFruitSerializer, tree).shouldEqual(treeSerialized)
    }

    //@Test
    fun `tree should be deserialized`() {
        json.parse(sealedifiedFruitSerializer, treeSerialized).shouldEqual(tree)
    }
}
