package dev.langchain4j.service

import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import java.lang.reflect.Method
import java.util.Optional

/**
 * This is a hack to access package-private methods in [DefaultAiServices].
 * It is not supposed to be used directly.
 */
internal object DefaultAiServicesOpener {
    /**
     * This class is used to open package-private methods in [DefaultAiServices].
     * It is not supposed to be used directly.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun findMemoryId(
        method: Method,
        args: Array<Any?>,
    ): Optional<ChatMemoryId> {
        val findMemoryId =
            DefaultAiServices::class.java.getDeclaredMethod(
                "findMemoryId",
                Method::class.java,
                Array<Any?>::class.java,
            )
        findMemoryId.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return findMemoryId.invoke(null, method, args) as Optional<Any>
    }
}
