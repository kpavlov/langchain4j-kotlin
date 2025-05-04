package me.kpavlov.langchain4j.kotlin.service.invoker

import dev.langchain4j.internal.VirtualThreadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Blocking
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation

public class HybridVirtualThreadInvocationHandler(
    private val virtualThreadExecutor: Executor = VirtualThreadUtils.createVirtualThreadExecutor {
        Executors.newCachedThreadPool()
    }!!,
    private val scope: CoroutineScope = CoroutineScope(virtualThreadExecutor.asCoroutineDispatcher()),
    private val executeSuspend: suspend (method: Method, args: Array<out Any>?) -> Any?,
    private val executeBlocking: (method: Method, args: Array<out Any>?) -> Any?
) : InvocationHandler {

    // Create a dispatcher backed by virtual threads (requires Java 21+)
    private val virtualThreadDispatcher =
        virtualThreadExecutor.asCoroutineDispatcher()

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        // Check if the method is suspended (last parameter is Continuation)
        val isSuspend = method.parameterTypes.lastOrNull()?.let {
            Continuation::class.java.isAssignableFrom(it)
        } ?: false

        // Check if method is annotated with @Blocking
        val isBlocking = method.isAnnotationPresent(Blocking::class.java)

        return when {
            isSuspend -> {
                // Handle suspend function (return Unit, actual result goes to continuation)
                @Suppress("UNCHECKED_CAST")
                val continuation = args?.last() as? Continuation<Any?>
                val actualArgs = args?.dropLast(1)?.toTypedArray()

                scope.launch {
                    @Suppress("TooGenericExceptionCaught")
                    try {
                        // Execute the method, using virtual thread dispatcher if blocking
                        val result = if (isBlocking) {
                            withContext(virtualThreadDispatcher) {
                                executeSuspend(method, actualArgs)
                            }
                        } else {
                            executeSuspend(method, actualArgs)
                        }
                        continuation?.resumeWith(Result.success(result))
                    } catch (e: Exception) {
                        continuation?.resumeWith(Result.failure(e))
                    }
                }

                kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
            }
            // Handle CompletableFuture or CompletionStage
            CompletionStage::class.java.isAssignableFrom(method.returnType) -> {
                scope.future {
                    if (isBlocking) {
                        withContext(virtualThreadDispatcher) {
                            executeSuspend(method, args)
                        }
                    } else {
                        executeSuspend(method, args)
                    }
                }
            }
            // Handle regular synchronous methods
            else -> {
                // For synchronous methods, run on virtual thread if blocking
                if (isBlocking) {
                    // Run blocking operation on virtual thread and wait for result
                    CompletableFuture.supplyAsync(
                        { executeBlocking(method, args) },
                        virtualThreadExecutor
                    ).join()
                } else {
                    // For regular non-blocking synchronous methods
                    executeBlocking(method, args)
                }
            }
        }
    }

}
