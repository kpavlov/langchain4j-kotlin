package me.kpavlov.langchain4j.kotlin.model.chat

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class ChatLanguageModelExtensionsKtTest {
    @Mock
    private lateinit var mockModel: ChatLanguageModel

    @Mock
    private lateinit var request: ChatRequest

    @Mock
    private lateinit var requestBuilder: ChatRequest.Builder

    @Mock
    private lateinit var expectedResponse: ChatResponse

    /**
     * This class tests the `chatAsync` extension function of `ChatLanguageModel`.
     * The function takes a `ChatRequest` or a `ChatRequest.Builder` as input,
     * performs asynchronous processing, and returns a `ChatResponse`.
     */

    @Test
    fun `chatAsync should return expected ChatResponse when using ChatRequest`() =
        runTest {
            whenever(mockModel.chat(request)).thenReturn(expectedResponse)

            val actualResponse = mockModel.chatAsync(request)

            assertThat(actualResponse).isEqualTo(expectedResponse)
            verify(mockModel).chat(request)
        }

    @Test
    fun `generateAsync should return expected AiMessage when using valid ChatMessages`() =
        runTest {
            val messages = mock<List<ChatMessage>>()
            val expectedAiMessage = mock<Response<AiMessage>>()

            whenever(mockModel.generate(messages)).thenReturn(expectedAiMessage)

            val actualAiMessage = mockModel.generateAsync(messages)

            assertThat(actualAiMessage).isSameInstanceAs(expectedAiMessage)
        }

    @Test
    fun `chatAsync should return expected ChatResponse when using ChatRequest Builder`() =
        runTest {
            whenever(requestBuilder.build()).thenReturn(request)
            whenever(mockModel.chat(request)).thenReturn(expectedResponse)

            val actualResponse = mockModel.chatAsync(requestBuilder)

            assertThat(actualResponse).isEqualTo(expectedResponse)
            verify(mockModel).chat(request)
        }

    @Test
    fun `chat should return expected ChatResponse when using ChatRequest Builder`() =
        runTest {
            whenever(requestBuilder.build()).thenReturn(request)
            whenever(mockModel.chat(request)).thenReturn(expectedResponse)

            val actualResponse = mockModel.chat(requestBuilder)

            assertThat(actualResponse).isEqualTo(expectedResponse)
            verify(mockModel).chat(request)
        }
}