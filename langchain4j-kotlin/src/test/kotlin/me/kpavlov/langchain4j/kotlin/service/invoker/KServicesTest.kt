package me.kpavlov.langchain4j.kotlin.service.invoker

import dev.langchain4j.internal.VirtualThreadUtils
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executors

internal class KServicesTest {
    internal interface TestService {
        suspend fun suspendMethod(param: String): String

        fun syncMethod(param: String): String

        fun completionStageMethod(param: String): CompletionStage<String>

        fun completableFutureMethod(param: String): CompletableFuture<String>
    }

    @Test
    fun `should handle suspend methods`(): Unit =
        runTest {
            // Given
            val service = KServices.create<TestService>(TestService::class)

            // When
            val result = service.suspendMethod("test")

            // Then
            result shouldBe "{param=test}"
        }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_19, disabledReason = "Requires Java 19 or higher")
    fun `should handle sync methods in virtual thread`() {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When - Run in a virtual thread
        val executor =
            VirtualThreadUtils.createVirtualThreadExecutor {
                Executors.newSingleThreadExecutor()
            }!!
        val result =
            executor
                .submit<String> {
                    service.syncMethod("test")
                }.get()

        // Then
        result shouldBe "{param=test}"
    }

    @Test
    fun `should fail sync methods in non-virtual thread`() {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When/Then - Should throw IllegalStateException when not in a virtual thread
        shouldThrow<IllegalStateException> {
            service.syncMethod("test")
        }
    }

    @Test
    fun `should handle completion stage methods`() {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When
        val future =
            service
                .completionStageMethod("test")
                .toCompletableFuture()

        // Then
        future.join() shouldBe "{param=test}"
    }

    @Test
    fun `should handle CompletableFuture methods`() {
        // Given
        val service = KServices.create<TestService>(TestService::class)

        // When
        val future = service.completableFutureMethod("test")

        // Then
        future.join() shouldBe "{param=test}"
    }
}
