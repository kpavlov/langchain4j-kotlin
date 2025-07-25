package me.kpavlov.langchain4j.kotlin.adapters

import assertk.assertThat
import assertk.assertions.startsWith
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.UserName
import dev.langchain4j.service.V
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply.CompleteResponse
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply.PartialResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class ServiceWithFlowTest {
    @Mock
    private lateinit var model: StreamingChatModel

    @Test
    fun `Should use TokenStreamToStringFlowAdapter`() =
        runTest {
            val partialToken1 = "Hello"
            val partialToken2 = "world"
            val completeResponse = ChatResponse.builder().aiMessage(AiMessage("Hello")).build()

            doAnswer {
                val handler = it.arguments[1] as StreamingChatResponseHandler
                handler.onPartialResponse(partialToken1)
                handler.onPartialResponse(partialToken2)
                handler.onCompleteResponse(completeResponse)
            }.whenever(model).chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())

            val assistant =
                AiServices
                    .builder(Assistant::class.java)
                    .streamingChatModel(model)
                    .build()

            val result =
                assistant
                    .askQuestion(userName = "My friend", question = "How are you?")
                    .toList()

            result shouldContainExactly listOf(partialToken1, partialToken2)
        }

    @Test
    fun `Should use TokenStreamToStringFlowAdapter error`() =
        runTest {
            val partialToken1 = "Hello"
            val partialToken2 = "world"
            val error = RuntimeException("Test error")

            doAnswer {
                val handler = it.arguments[1] as StreamingChatResponseHandler
                handler.onPartialResponse(partialToken1)
                handler.onPartialResponse(partialToken2)
                handler.onError(error)
            }.whenever(model).chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())

            val assistant =
                AiServices
                    .builder(Assistant::class.java)
                    .streamingChatModel(model)
                    .build()

            val response =
                assistant
                    .askQuestion(userName = "My friend", question = "How are you?")
                    .catch {
                        val message =
                            requireNotNull(
                                it.message,
                            ) { "Only $error is allowed to occur here but found $it" }
                        emit(message)
                    }.toList()

            response shouldContainExactly listOf(partialToken1, partialToken2, error.message)
        }

    @Test
    fun `Should use TokenStreamToReplyFlowAdapter`() =
        runTest {
            val partialToken1 = "Hello"
            val partialToken2 = "world"
            val completeResponse = ChatResponse.builder().aiMessage(AiMessage("Hello")).build()

            doAnswer {
                val handler = it.arguments[1] as StreamingChatResponseHandler
                handler.onPartialResponse(partialToken1)
                handler.onPartialResponse(partialToken2)
                handler.onCompleteResponse(completeResponse)
            }.whenever(model).chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())

            val assistant =
                AiServices
                    .builder(Assistant::class.java)
                    .streamingChatModel(model)
                    .build()

            val result =
                assistant
                    .askQuestion2(userName = "My friend", question = "How are you?")
                    .toList()

            assertThat(
                result,
            ).startsWith(PartialResponse(partialToken1), PartialResponse(partialToken2))
            assertTrue(result[2] is CompleteResponse)
        }

    @Test
    fun `Should use TokenStreamToReplyFlowAdapter error`() =
        runTest {
            val partialToken1 = "Hello"
            val partialToken2 = "world"
            val error = RuntimeException("Test error")

            doAnswer {
                val handler = it.arguments[1] as StreamingChatResponseHandler
                handler.onPartialResponse(partialToken1)
                handler.onPartialResponse(partialToken2)
                handler.onError(error)
            }.whenever(model).chat(any<ChatRequest>(), any<StreamingChatResponseHandler>())

            val assistant =
                AiServices
                    .builder(Assistant::class.java)
                    .streamingChatModel(model)
                    .build()

            val results = mutableListOf<String>()

            val exception =
                shouldThrow<Exception> {
                    assistant
                        .askQuestion2(userName = "My friend", question = "How are you?")
                        .onEach {
                            it.shouldBeInstanceOf<StreamingChatModelReply.PartialResponse> {
                                results.add(
                                    it.token,
                                )
                            }
                        }.toList()
                }
            exception.message shouldBe "Test error"

            results shouldHaveSize 2
            assertThat(
                results,
            ).startsWith(partialToken1, partialToken2)
        }

    @Suppress("unused")
    private interface Assistant {
        @dev.langchain4j.service.UserMessage(
            "Hello, I am {{ userName }}. {{ message }}.",
        )
        fun askQuestion(
            @UserName userName: String,
            @V("message") question: String,
        ): Flow<String>

        @dev.langchain4j.service.UserMessage(
            "Hello, I am {{ userName }}. {{ message }}.",
        )
        fun askQuestion2(
            @UserName userName: String,
            @V("message") question: String,
        ): Flow<StreamingChatModelReply>
    }
}
