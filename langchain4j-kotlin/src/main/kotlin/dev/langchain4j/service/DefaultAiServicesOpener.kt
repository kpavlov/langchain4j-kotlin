package dev.langchain4j.service

import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.InvocationHandler
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.Optional
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.Consumer
import kotlin.coroutines.Continuation

internal class DefaultAiServicesOpener<T : Any>(context: AiServiceContext) {

    private val defaultAiServices = DefaultAiServices<T>(context)

    /**
     * This class is used to open package-private methods in [DefaultAiServices].
     * It is not supposed to be used directly.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun findMemoryId(
        method: Method,
        args: Array<Any?>
    ): Optional<ChatMemoryId> {
        val findMemoryId = DefaultAiServices::class.java.getDeclaredMethod(
            "findMemoryId",
            Method::class.java,
            Array<Any?>::class.java
        )
        findMemoryId.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return findMemoryId.invoke(null, method, args) as Optional<Any>
    }


    internal fun validateParameters(
        method: Method
    ) {
        val validateParameters = DefaultAiServices::class.java.getDeclaredMethod(
            "validateParameters",
            Method::class.java
        )
        validateParameters.isAccessible = true
        try {
            validateParameters.invoke(null, method)
        } catch (e: Exception) {
            if (e.cause is dev.langchain4j.service.IllegalConfigurationException) {
                val illegalConfigurationException =
                    e.cause as dev.langchain4j.service.IllegalConfigurationException
                illegalConfigurationException.printStackTrace()
                return
            }
            throw e
        }
    }

    internal fun prepareUserMessage(
        method: Method,
        args: Array<Any?>
    ): UserMessage {
        val prepareUserMessage = DefaultAiServices::class.java.getDeclaredMethod(
            "prepareUserMessage",
            Method::class.java,
            Array<Any?>::class.java
        )
        prepareUserMessage.isAccessible = true
        return prepareUserMessage.invoke(null, method, args) as UserMessage
    }

    internal fun prepareSystemMessage(
        memoryId: Any?,
        method: Method,
        args: Array<Any?>
    ): SystemMessage? {
        val prepareSystemMessage = DefaultAiServices::class.java.getDeclaredMethod(
            "prepareSystemMessage",
            Any::class.java,
            Method::class.java,
            Array<Any?>::class.java
        )
        prepareSystemMessage.isAccessible = true
        //val callArgs = args.filter { it !is Continuation<*> }
        @Suppress("UNCHECKED_CAST")
        val result = prepareSystemMessage.invoke(
            defaultAiServices,
            memoryId, method, args
        ) as Optional<SystemMessage>
        return result.orElse(null)
    }
}
