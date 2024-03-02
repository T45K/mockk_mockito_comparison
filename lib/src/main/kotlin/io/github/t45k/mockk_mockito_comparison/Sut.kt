package io.github.t45k.mockk_mockito_comparison

class Sut {
    fun hello(): WrapObject {
        TODO()
    }

    suspend fun helloAsync(): WrapObject {
        TODO()
    }

    fun args(value: WrapObject) {
        TODO()
    }

    fun valueClass(value: ValueClass): String {
        TODO()
    }
}

data class WrapObject(val value: String)

@JvmInline
value class ValueClass(val value: String)

object Object {
    @JvmStatic
    fun helloWorld() = "hello world"
}

fun topLevelFunction() {
    TODO()
}
