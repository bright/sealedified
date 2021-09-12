package pl.brightinventions.sealedified

/*
class SealedifiedSerializer<T : Any, U : Any>(
    private val knownSerializer: KSerializer<T>,
    private val unknownSerializer: KSerializer<U>
) : KSerializer<Sealedified<T, U>> {

    override val descriptor: SerialDescriptor = knownSerializer.descriptor

    override fun deserialize(decoder: Decoder): Sealedified<T, U> {
        if (decoder !is JsonDecoder) {
            throw SerializationException("${SealedifiedSerializer::class.simpleName} only supports JSON serialization")
        }

        val jsonElement = decoder.decodeJsonElement()

        return try {
            val known = decoder.json.decodeFromJsonElement(knownSerializer, jsonElement)
            SealedifiedJson.Known(known)
        } catch (e: SerializationException) {
            if (jsonElement !is JsonObject) {
                throw SerializationException(
                    "${SealedifiedSerializer::class.simpleName} only supports ${JsonObject::class.simpleName}",
                    e
                )
            }
            SealedifiedJson.Unknown(jsonElement)
        }
    }

    override fun serialize(encoder: Encoder, value: Sealedified<T, U>) {
        when (value) {
            is Sealedified.Known -> encoder.encodeSerializableValue(knownSerializer, value.value)
            is Sealedified.Unknown -> encoder.encodeSerializableValue(JsonObject.serializer(), value.jsonObject)
        }
    }
}

inline fun <reified T : Any> sealedifiedSerializer(other: KSerializer<T>): KSerializer<SealedifiedJson<T>> =
    SealedifiedSerializer(other)
*/
