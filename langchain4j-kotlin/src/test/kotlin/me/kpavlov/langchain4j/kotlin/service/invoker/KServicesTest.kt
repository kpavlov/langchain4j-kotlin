package me.kpavlov.langchain4j.kotlin.service.invoker

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

internal class KServicesTest {

    internal interface TestService {
        public suspend fun suspendMethod(param: String): String
        public fun syncMethod(param: String): String
        public fun completionStageMethod(param: String): CompletionStage<String>
        public fun completableFutureMethod(param: String): CompletableFuture<String>
    }

    @Test
    public fun `should handle suspend methods`(): Unit = runTest {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When
        val result = service.suspendMethod("test")

        // Then
        assertEquals("{param=test}", result)
    }

    @Test
    public fun `should handle sync methods`(): Unit {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When
        val result = service.syncMethod("test")

        // Then
        assertEquals("{param=test}", result)
    }

    @Test
    public fun `should handle completion stage methods`(): Unit {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When
        val future = service.completionStageMethod("test")
            .toCompletableFuture()

        // Then
        assertEquals("{param=test}", future.join())
    }

    @Test
    public fun `should handle CompletableFuture methods`(): Unit {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When
        val future = service.completableFutureMethod("test")

        // Then
        assertEquals("{param=test}", future.join())
    }
}
