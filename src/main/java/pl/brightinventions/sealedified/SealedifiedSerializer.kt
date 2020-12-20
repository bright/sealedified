package pl.brightinventions.sealedified

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject

class SealedifiedSerializer<T : Any>(
    private val knownSerializer: KSerializer<T>
) : KSerializer<Sealedified<T>> {

    override val descriptor: SerialDescriptor = knownSerializer.descriptor

    override fun deserialize(decoder: Decoder): Sealedified<T> {
        if (decoder !is JsonInput) {
            throw SerializationException("${SealedifiedSerializer::class.simpleName} only supports JSON serialization")
        }

        val jsonElement = decoder.decodeJson()

        return try {
            // Copy the original content first because the class discriminator may get removed from the map.
            val jsonObject = jsonElement.jsonObject
            val copiedMap = HashMap(jsonObject)
            val copiedJsonObject = JsonObject(copiedMap)
            val known = decoder.json.fromJson(knownSerializer, copiedJsonObject)
            Sealedified.Known(known)
        } catch (e: Exception) {
            if (jsonElement !is JsonObject) {
                throw SerializationException(
                    "${SealedifiedSerializer::class.simpleName} only supports ${JsonObject::class.simpleName}",
                    e
                )
            }
            Sealedified.Unknown(jsonElement)
        }
    }

    override fun serialize(encoder: Encoder, value: Sealedified<T>) {
        when (value) {
            is Sealedified.Known -> encoder.encodeSerializableValue(knownSerializer, value.value)
            is Sealedified.Unknown -> encoder.encodeSerializableValue(JsonObject.serializer(), value.jsonObject)
        }
    }
}

inline fun <reified T : Any> sealedifiedSerializer(other: KSerializer<T>): KSerializer<Sealedified<T>> =
    SealedifiedSerializer(other)
