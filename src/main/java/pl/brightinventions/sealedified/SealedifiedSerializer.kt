package pl.brightinventions.sealedified

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

class SealedifiedSerializer<T : Any>(
    private val knownSerializer: KSerializer<T>
) : KSerializer<Sealedified<T>> {

    override val descriptor: SerialDescriptor = knownSerializer.descriptor

    override fun deserialize(decoder: Decoder): Sealedified<T> {
        if (decoder !is JsonDecoder) {
            throw SerializationException("${SealedifiedSerializer::class.simpleName} only supports JSON serialization")
        }

        val jsonElement = decoder.decodeJsonElement()

        return try {
            val known = decoder.json.decodeFromJsonElement(knownSerializer, jsonElement)
            Sealedified.Known(known)
        } catch (e: SerializationException) {
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
