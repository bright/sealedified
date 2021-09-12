package pl.brightinventions.sealedified

sealed class Sealedified<T : Any, U : Any?> {

    data class Known<T : Any, U : Any?>(val value: T) : Sealedified<T, U>()

    abstract class Unknown<T : Any, U : Any?> : Sealedified<T, U>() {
        abstract val raw: U
    }

    fun knownOrNull() = (this as? Known<T, U>)?.value
}

