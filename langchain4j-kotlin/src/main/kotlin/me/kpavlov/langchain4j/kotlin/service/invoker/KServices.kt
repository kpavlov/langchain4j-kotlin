package me.kpavlov.langchain4j.kotlin.service.invoker

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass

/**
 * Utility class for creating dynamic proxies for AI services.
 */
internal object KServices {
    private val logger = LoggerFactory.getLogger(KServices::class.java)

    /**
     * Checks if the current thread is a virtual thread.
     *
     * @return true if the current thread is a virtual thread, false otherwise
     */
    private fun isVirtualThread(): Boolean {
        // Use reflection to check if Thread.currentThread().isVirtual() exists and call it
        return try {
            val isVirtualMethod = Thread::class.java.getMethod("isVirtual")
            isVirtualMethod.invoke(Thread.currentThread()) as Boolean
        } catch (e: Exception) {
            // If the method doesn't exist or fails, assume it's not a virtual thread
            false
        }
    }

    /**
     * Ensures that the current thread is a virtual thread.
     *
     * @throws IllegalStateException if the current thread is not a virtual thread
     */
    private fun ensureVirtualThread() {
        check(isVirtualThread()) {
            "Synchronous methods must be executed in a virtual thread. " +
                "Use Executors.newVirtualThreadPerTaskExecutor() " +
                "or similar to create virtual threads."
        }
    }


    /**
     * Creates a dynamic proxy for the given service class.
     *
     * @param serviceClass The service class to create a proxy for
     * @return A proxy instance of the service class
     */
    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T : Any> create(serviceClass: KClass<T>): T {
        ServiceClassValidator.validateClass(serviceClass)

        val executor = AiServiceOrchestrator(serviceClass)

        // Create a HybridVirtualThreadInvocationHandler that handles both suspend and blocking operations
        val handler = HybridVirtualThreadInvocationHandler(
            // Handle suspend functions
            executeSuspend = { method, args ->
                // Extract parameters from args
                val params = extractParameters(method, args)

                // Execute the method using the executor
                executor.execute<Any>(method, params)
            },
            // Handle blocking functions
            executeBlocking = { method, args ->
                // Handle Object methods
                when {
                    method.name == "toString" -> {
                        return@HybridVirtualThreadInvocationHandler "Dynamic proxy for $serviceClass"
                    }

                    method.declaringClass == Any::class.java -> {
                        return@HybridVirtualThreadInvocationHandler method.invoke(this, args)
                    }
                }

                // Extract parameters from args
                val params = extractParameters(method, args)

                // For void methods, launch a coroutine and return null
                if (method.returnType == Void.TYPE) {
                    GlobalScope.launch(Dispatchers.IO) {
                        executor.execute<Any>(method, params)
                    }
                    return@HybridVirtualThreadInvocationHandler null
                }

                // For other methods, ensure we're running in a virtual thread and execute synchronously
                ensureVirtualThread()

                runBlocking {
                    executor.execute<Any>(method, params)
                }
            }
        )

        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
            handler,
        ) as T
    }


    /**
     * Extracts parameters from method arguments.
     */
    private fun extractParameters(
        method: Method,
        args: Array<out Any>?,
    ): Map<String, Any?> {
        val params = mutableMapOf<String, Any?>()

        if (args != null) {
            for (i in args.indices) {
                val arg = args[i]
                if (arg is Continuation<*>) continue
                val paramName = method.parameters[i].name ?: "param$i"
                params[paramName] = arg
            }
        }

        return params
    }

    /**
     * Gets the return type of a method, handling both regular and suspend methods.
     */
    @Suppress("unused")
    private fun getReturnType(method: Method): Type {
        // For suspend functions, check if the last parameter is a Continuation
        val parameters = method.genericParameterTypes
        if (parameters.isNotEmpty()) {
            val lastParam = parameters.last()
            if (lastParam is ParameterizedType &&
                (lastParam.rawType as? Class<*>)?.name == Continuation::class.java.name
            ) {
                // Return the type argument of the Continuation
                return lastParam.actualTypeArguments[0]
            }
        }

        // For regular functions or functions returning CompletionStage
        val returnType = method.genericReturnType

        // If it's a CompletionStage, return its type parameter
        if (returnType is ParameterizedType &&
            CompletionStage::class.java.isAssignableFrom(returnType.rawType as Class<*>)
        ) {
            return returnType.actualTypeArguments[0]
        }

        // If it's a CompletionStage, return its type parameter
        if (returnType is ParameterizedType &&
            CompletableFuture::class.java.isAssignableFrom(returnType.rawType as Class<*>)
        ) {
            return returnType.actualTypeArguments[0]
        }

        return returnType
    }
}
