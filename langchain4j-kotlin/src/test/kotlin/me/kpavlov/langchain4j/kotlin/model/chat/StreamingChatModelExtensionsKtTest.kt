package me.kpavlov.langchain4j.kotlin.model.chat

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.AiMessage.aiMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply.CompleteResponse
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply.PartialResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class StreamingChatModelExtensionsKtTest {
    @Mock
    private lateinit var mockModel: StreamingChatModel

    @Test
    fun `chatFlow should handle partial and complete responses`() =
        runTest {
            val partialToken1 = "Hello"
            val partialToken2 = "world"
            val completeResponse = ChatResponse.builder().aiMessage(AiMessage("Hello")).build()

            // Simulate the streaming behavior with a mocked handler
            doAnswer {
                val handler = it.arguments[1] as StreamingChatResponseHandler
                handler.onPartialResponse(partialToken1)
                handler.onPartialResponse(partialToken2)
                handler.onCompleteResponse(completeResponse)
            }.whenever(mockModel).chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())

            val flow =
                mockModel.chatFlow {
                    messages += userMessage("Hey, there!")
                }
            val result = flow.toList()

            // Assert partial responses
            result shouldContainExactly
                listOf(
                    PartialResponse(partialToken1),
                    PartialResponse(partialToken2),
                    CompleteResponse(completeResponse),
                )

            // Verify interactions
            verify(mockModel).chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())
        }

    @Test
    fun `chatFlow should respect buffering strategy`() =
        runTest {
            val partialToken0 = "start"
            val partialToken1 = "hello"
            val partialToken2 = "world"
            val completeResponse =
                ChatResponse
                    .builder()
                    .aiMessage(aiMessage("Done"))
                    .build()

            // Simulate the streaming behavior with a mocked handler
            doAnswer {
                val handler = it.arguments[1] as StreamingChatResponseHandler
                handler.onPartialResponse(partialToken0)
                handler.onPartialResponse(partialToken1)
                handler.onPartialResponse(partialToken2)
                handler.onCompleteResponse(completeResponse)
            }.whenever(mockModel)
                .chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())

            val result = mutableListOf<StreamingChatModelReply>()
            mockModel
                .chatFlow(
                    bufferCapacity = 1,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                ) {
                    messages += userMessage("Hey, there!")
                }.onEach {
                    println(it)
                    delay(100)
                }.collect {
                    result.add(it)
                }

            // Assert partial responses
            result shouldContainExactly
                listOf(
                    PartialResponse(partialToken0),
                    CompleteResponse(completeResponse),
                )

            // Verify interactions
            verify(mockModel)
                .chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())
        }

    @Test
    fun `chatFlow should handle errors`() =
        runTest {
            val error = RuntimeException("Test error")

            // Simulate the error during streaming
            doAnswer {
                val handler = it.arguments[1] as StreamingChatResponseHandler
                handler.onError(error)
            }.whenever(mockModel).chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())

            val flow =
                mockModel.chatFlow {
                    messages += userMessage("Hey, there!")
                }

            val exception =
                shouldThrow<RuntimeException> {
                    flow.toList()
                }

            exception shouldHaveMessage "Test error"
        }
}
