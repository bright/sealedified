package pl.brightinventions.sealedified.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import pl.brightinventions.sealedified.Sealedified

class SealedifiedJsonSerializer<T : Any>(
    private val knownSerializer: KSerializer<T>
) : KSerializer<Sealedified<T, JsonObject>> {

    override val descriptor: SerialDescriptor = knownSerializer.descriptor

    override fun deserialize(decoder: Decoder): Sealedified<T, JsonObject> {
        if (decoder !is JsonDecoder) {
            throw SerializationException("${SealedifiedJsonSerializer::class.simpleName} only supports JSON serialization")
        }

        val jsonElement = decoder.decodeJsonElement()

        return try {
            val known = decoder.json.decodeFromJsonElement(knownSerializer, jsonElement)
            Sealedified.Known(known)
        } catch (e: SerializationException) {
            if (jsonElement !is JsonObject) {
                throw SerializationException(
                    "${SealedifiedJsonSerializer::class.simpleName} only supports ${JsonObject::class.simpleName}",
                    e
                )
            }
            UnknownJson(jsonElement)
        }
    }

    override fun serialize(encoder: Encoder, value: Sealedified<T, JsonObject>) {
        when (value) {
            is Sealedified.Known -> encoder.encodeSerializableValue(knownSerializer, value.value)
            is Sealedified.Unknown -> {
                val raw = try {
                    (value as UnknownJson<T>).raw
                } catch (e: TypeCastException) {
                    throw SerializationException(
                        "${SealedifiedJsonSerializer::class.simpleName} only supports ${UnknownJson::class.simpleName}",
                        e
                    )
                }
                encoder.encodeSerializableValue(JsonObject.serializer(), raw)
            }
        }
    }
}

inline fun <reified T : Any> sealedifiedJsonSerializer(other: KSerializer<T>): KSerializer<Sealedified<T, JsonObject>> =
    SealedifiedJsonSerializer(other)
