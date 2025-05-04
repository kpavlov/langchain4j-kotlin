package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.service.IllegalConfigurationException
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.UserName
import dev.langchain4j.service.V
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import me.kpavlov.langchain4j.kotlin.service.invoker.HybridVirtualThreadInvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation

@OptIn(DelicateCoroutinesApi::class)
internal object ReflectionHelper {
    private val vtDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

    fun validateParameters(method: Method) {
        val parameters = method.getParameters()
        if (parameters == null || parameters.size < 2) {
            return
        }

        for (parameter in parameters) {
            if ("$parameter".startsWith("kotlin.coroutines.Continuation")) {
                // skip continuation parameter
                continue
            }
            val v = parameter.getAnnotation<V>(V::class.java)
            val userMessage =
                parameter.getAnnotation<UserMessage>(UserMessage::class.java)
            val memoryId = parameter.getAnnotation<MemoryId?>(MemoryId::class.java)
            val userName = parameter.getAnnotation<UserName?>(UserName::class.java)
            @Suppress("ComplexCondition")
            if (v == null && userMessage == null && memoryId == null && userName == null) {
                throw IllegalConfigurationException.illegalConfiguration(
                    "Parameter '%s' of method '%s' should be annotated with @V or @UserMessage " +
                        "or @UserName or @MemoryId",
                    parameter.getName(),
                    method.getName(),
                )
            }
        }
    }

    @Throws(kotlin.IllegalStateException::class)
    private fun getReturnType(method: Method): Type {
        val continuationParam = method.parameterTypes.findLast { it.kotlin is Continuation<*> }
        if (continuationParam != null) {
            @Suppress("UseCheckOrError")
            return continuationParam.genericInterfaces[0]
                ?: throw IllegalStateException(
                    "Can't find generic interface of continuation parameter",
                )
        }
        return method.getGenericReturnType()
    }

    fun getSuspendReturnType(method: Method): java.lang.reflect.Type {
        val parameters = method.genericParameterTypes
        if (parameters.isEmpty()) return getReturnType(method)
        val lastParameter = parameters.last()
        // Check if the last parameter is Continuation<T>
        return (
            if (lastParameter is ParameterizedType &&
                (lastParameter.rawType as? Class<*>)?.name == Continuation::class.java.name
            ) {
                // T is the first (and only) type argument
                val type = lastParameter.actualTypeArguments.first()
                if (type is WildcardType) {
                    type.lowerBounds.first()
                } else {
                    type
                }
            } else {
                getReturnType(method) // Not a suspend function, or not detectably so
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> createSuspendProxy(
        iface: Class<T>,
        handler: suspend (method: java.lang.reflect.Method, args: Array<Any?>) -> Any?,
    ): T {
        // Create a HybridVirtualThreadInvocationHandler that uses the provided handler
        // for both suspend and blocking operations
        val invocationHandler =
            HybridVirtualThreadInvocationHandler(
                executeSuspend = { method, args ->
                    handler(method, args as Array<Any?>)
                },
                executeSync = { method, args ->
                    // For blocking operations, we run the suspend handler in a blocking context
                    runBlocking {
                        handler(method, args as Array<Any?>)
                    }
                },
            )

        return Proxy.newProxyInstance(
            iface.classLoader,
            arrayOf(iface),
            invocationHandler,
        ) as T
    }

    internal fun dropContinuationArg(args: Array<Any?>): Array<Any?> =
        args
            .dropLastWhile {
                it is Continuation<*>
            }.toTypedArray()
}
