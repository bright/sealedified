[![](https://jitpack.io/v/bright/sealedified.svg)](https://jitpack.io/#bright/sealedified)

# sealedified #

Forward compatible sealed class polymorphic serialization with
[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/).

It allows you to properly deserialize an unknown type of object without
deserialization errors e.g. to cache the raw data and use it later,
possibly after updating your application in a way that adds the missing
type support.

This project is very similar to
[codified enums](https://github.com/bright/codified) that adds forward
compatible enums serialization which is also explained
[here](https://brightinventions.pl/blog/forward-compatible-enums-in-kotlin).

## Installation ##

First, add JitPack to your repositories block in Gradle build script.

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

Then, add the following dependencies:

```kotlin
implementation("com.github.bright.sealedified:sealedified:1.6.10")
implementation("com.github.bright.sealedified:sealedified-json:1.6.10")
```

### Exemplary usage with JSON serialization ###

Normally, when you have a sealed class like this:

```kotlin
@Serializable
sealed class Fruit {

    @Serializable
    data class Apple(val size: Int) : Fruit()

    @Serializable
    data class Orange(val owner: String?) : Fruit()
}
```

and you try to deserialize some type that is not a part of your sealed
class hierarchy (yet!), such as `Banana`:

```json
{
    "type": "banana",
    "length": 10.0
}
```

using the default polymorphic serializer generated for your sealed class
`Fruit`:

```kotlin
val unknownBananaSerialized =
    """
        {
            "type": "banana",
            "length": 10.0
        }
    """.trimIndent()
Json.decodeFromString(Fruit.serializer(), unknownBananaSerialized)
```

you will get an error such as `SerializationException` because `"type":
"banana"` doesn't have a corresponding type in your sealed class
hierarchy.

`Sealedified` is a sealed class that represents either "known" or
"unknown" type. It can either wrap some data type your application
recognizes or something that is unknown yet and can be supported in the
future when you extend your implementation.

For each type you want to wrap with `Sealedified` you must first create
a custom serializer this way:

```kotlin
object SealedifiedFruitSerializer : KSerializer<Sealedified<Fruit, JsonObject>>
    by SealedifiedJsonSerializer(Fruit.serializer())
```

and use it instead of the default generated serializer, e.g.

```kotlin
Json.decodeFromString(SealedifiedFruitSerializer, unknownBananaSerialized)
```

or, if your object is nested in another serializable class, you can use
`@Serializable` annotation like this:

```kotlin
@Serializable
data class FruitWrapper(
    @Serializable(with = SealedifiedFruitSerializer::class)
    val fruit: Sealedified<Fruit, JsonObject>?
)
```

However, if you have a collection such as `List`, remember to apply the
annotation to `Sealedified` type - inside the collection:

```kotlin
@Serializable
data class FruitBasket(
    val fruits: List<@Serializable(with = Fruit.SealedifiedSerializer::class) Sealedified<Fruit, JsonObject>>
)
```

Thanks to that, you will be able to handle the unknown types using
`when` expressions like this:

```kotlin
val sealedifiedFruit: Sealedified<Fruit, JsonObject> = Json.decodeFromString(SealedifiedFruitSerializer, someFruitSerialized)

when (sealedifiedFruit) {
    is Sealedified.Known -> when (val knownValue = sealedifiedFruit.value) {
        is Fruit.Apple -> println("Size: ${knownValue.size}")
        is Fruit.Orange -> println("Owner: ${knownValue.owner}")
    }
    is Sealedified.Unknown -> println("Raw JSON: ${sealedifiedFruit.raw}")
}
```

