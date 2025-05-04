package me.kpavlov.langchain4j.kotlin.service.invoker

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executors

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
        result shouldBe "{param=test}"
    }

    @Test
    public fun `should handle sync methods in virtual thread`(): Unit {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When - Run in a virtual thread
        val result = Executors.newVirtualThreadPerTaskExecutor().submit<String> {
            service.syncMethod("test")
        }.get()

        // Then
        result shouldBe "{param=test}"
    }

    @Test
    public fun `should fail sync methods in non-virtual thread`(): Unit {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When/Then - Should throw IllegalStateException when not in a virtual thread
        shouldThrow<IllegalStateException> {
            service.syncMethod("test")
        }
    }

    @Test
    public fun `should handle completion stage methods`(): Unit {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When
        val future = service.completionStageMethod("test")
            .toCompletableFuture()

        // Then
        future.join() shouldBe "{param=test}"
    }

    @Test
    public fun `should handle CompletableFuture methods`(): Unit {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When
        val future = service.completableFutureMethod("test")

        // Then
        future.join() shouldBe "{param=test}"
    }
}
