package me.kpavlov.langchain4j.kotlin.service.invoker

import kotlinx.coroutines.delay
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

/**
 * A simple service orchestrator that executes methods on a service.
 * This is a placeholder implementation that returns the parameters as a string.
 */
internal class AiServiceOrchestrator<T : Any>(
    @Suppress("UNUSED_PARAMETER") private val serviceClass: KClass<T>,
) {
    suspend fun <R : Any> execute(
        @Suppress("UNUSED_PARAMETER") method: Method,
        params: Map<String, Any?>,
    ): R? {
        delay(5.milliseconds)
        @Suppress("UNCHECKED_CAST")
        return params.toString() as? R?
    }
}
