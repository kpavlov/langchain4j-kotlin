package me.kpavlov.langchain4j.kotlin.service.invoker

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.jetbrains.annotations.Blocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.coroutines.Continuation

@ExtendWith(MockitoExtension::class)
internal class HybridVirtualThreadInvocationHandlerTest {
    private lateinit var mockScope: TestScope

    @Mock
    private lateinit var mockExecuteSuspend: suspend (Method, Array<out Any>?) -> Any?

    @Mock
    private lateinit var mockExecuteSync: (Method, Array<out Any>?) -> Any?
    private lateinit var handler: HybridVirtualThreadInvocationHandler

    @BeforeEach
    fun setUp() {
        mockScope = TestScope(StandardTestDispatcher())
        handler =
            HybridVirtualThreadInvocationHandler(
                scope = mockScope,
                executeSuspend = mockExecuteSuspend,
                executeSync = mockExecuteSync,
            )
    }

    @Test
    fun `should handle suspend function invocation`() {
        // Given
        val proxy = mock<Any>()
        val method = mock<Method>()
        val continuation = mock<Continuation<Any?>>()
        val args = arrayOf("arg1", continuation)

        // Configure a method to appear as a suspend function
        whenever(method.parameterTypes).thenReturn(
            arrayOf(
                String::class.java,
                Continuation::class.java,
            ),
        )

        // When
        val result = handler.invoke(proxy, method, args)

        // Then
        assertThat(result).isEqualTo(kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED)
    }

    @Test
    fun `should handle CompletionStage return type`() {
        // Given
        val proxy = mock<Any>()
        val method = mock<Method>()
        val args = arrayOf("arg1")

        // Configure a method to return CompletionStage
        whenever(method.returnType).thenReturn(CompletionStage::class.java)
        whenever(method.isAnnotationPresent(Blocking::class.java)).thenReturn(false)
        whenever(method.parameterTypes).thenReturn(arrayOf(String::class.java))

        // When
        val result = handler.invoke(proxy, method, args)

        // Then
        assertThat(result is CompletableFuture<*>).isTrue()
    }

    @Test
    fun `should handle synchronous blocking method`() {
        // Given
        val proxy = mock<Any>()
        val method = mock<Method>()
        val args = arrayOf("arg1")
        val expectedResult = "result"

        // Configure method as blocking
        whenever(method.returnType).thenReturn(String::class.java)
        whenever(method.isAnnotationPresent(Blocking::class.java)).thenReturn(true)
        whenever(method.parameterTypes).thenReturn(arrayOf(String::class.java))

        // Configure mock to return an expected result
        whenever(mockExecuteSync.invoke(method, args)).thenReturn(expectedResult)

        // When
        val result = handler.invoke(proxy, method, args)

        // Then
        assertThat(result).isEqualTo(expectedResult)
        verify(mockExecuteSync).invoke(method, args)
    }

    @Test
    fun `should handle synchronous non-blocking method`() {
        // Given
        val proxy = mock<Any>()
        val method = mock<Method>()
        val args = arrayOf("arg1")
        val expectedResult = "result"

        // Configure method as non-blocking
        whenever(method.returnType).thenReturn(String::class.java)
        whenever(method.isAnnotationPresent(Blocking::class.java)).thenReturn(false)
        whenever(method.parameterTypes).thenReturn(arrayOf(String::class.java))

        // Configure mock to return expected result
        whenever(mockExecuteSync.invoke(method, args)).thenReturn(expectedResult)

        // When
        val result = handler.invoke(proxy, method, args)

        // Then
        assertThat(result).isEqualTo(expectedResult)
        verify(mockExecuteSync).invoke(method, args)
    }
}
