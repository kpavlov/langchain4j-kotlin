package me.kpavlov.langchain4j.kotlin.service

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(DelicateCoroutinesApi::class)
internal object ReflectionHelper {
    private val vtDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

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
        return Proxy.newProxyInstance(
            iface.classLoader,
            arrayOf(iface),
            InvocationHandler { _, method, args ->
                // If not a suspend method, optionally fall back
                val cont =
                    args.lastOrNull() as? Continuation<Any?>
                        ?: return@InvocationHandler method.invoke(this, args)

                // Remove Continuation for our handler
                val argsForSuspend = args.dropLast(1).toTypedArray()

                // Launch coroutine for the suspend implementation
                // (here, for demonstration, using a helper)
                // Use coroutine machinery to start the suspend block
                // If using Kotlin 1.3+, this is the correct way
                // Uses GlobalScope (be sure that's okay for your use-case!)
                GlobalScope.launch(vtDispatcher) {
                    @Suppress("TooGenericExceptionCaught")
                    try {
                        val result = handler(method, argsForSuspend)
                        cont.resume(result)
                    } catch (e: Throwable) {
                        cont.resumeWithException(e)
                    }
                }
                COROUTINE_SUSPENDED
            },
        ) as T
    }

    @FunctionalInterface
    interface MyApi {
        suspend fun greet(name: String): String
    }

    internal fun dropContinuationArg(args: Array<Any?>): Array<Any?> =
        args
            .dropLastWhile {
                it is Continuation<*>
            }.toTypedArray()
}

public fun main() {
    val proxy =
        ReflectionHelper.createSuspendProxy(ReflectionHelper.MyApi::class.java) { method, args ->
            "${Thread.currentThread()}: Hello, $method(${args[0]} )"
        }

    runBlocking {
        println(proxy.greet("world")) // Prints: Hello, world
    }
}
