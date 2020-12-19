package pl.brightinventions.sealedified

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlin.reflect.KClass

@InternalSerializationApi
class SealedifiedSerializer<T : Any>(
    private val base: KClass<T>,
    private val knownSerializer: KSerializer<T>
) : AbstractPolymorphicSerializer<Sealedified<T>>(), KSerializer<Sealedified<T>> {

    override val descriptor: SerialDescriptor = knownSerializer.descriptor
    override val baseClass: KClass<Sealedified<T>>
        get() = Sealedified::class

    /*override fun deserialize(decoder: Decoder): Sealedified<T> =
        try {
            val known = knownSerializer.deserialize(decoder)
            Sealedified.Known(known)
        } catch (e: SerializationException) {
            val unknown = JsonObjectSerializer.deserialize(decoder)
            Sealedified.Unknown(unknown)
        }

    override fun serialize(encoder: Encoder, value: Sealedified<T>) {
        when (value) {
            is Sealedified.Known -> knownSerializer.serialize(encoder, value.value)
            is Sealedified.Unknown -> JsonObjectSerializer.serialize(encoder, value.jsonElement)
        }
    }*/
}

//inline fun <reified T> sealedifiedSerializer(): KSerializer<Sealedified<T>> = SealedifiedSerializer()
