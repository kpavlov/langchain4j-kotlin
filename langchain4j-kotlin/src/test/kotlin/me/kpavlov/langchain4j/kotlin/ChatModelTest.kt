package me.kpavlov.langchain4j.kotlin

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import kotlinx.coroutines.test.runTest
import me.kpavlov.langchain4j.kotlin.model.chat.chatAsync
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class ChatModelTest {
    @Mock
    lateinit var model: ChatModel

    @Captor
    lateinit var chatRequestCaptor: org.mockito.ArgumentCaptor<ChatRequest>

    @Mock
    lateinit var chatResponse: ChatResponse

    @Test
    fun `Should call chatAsync`() {
        val temp = 0.8
        runTest {
            whenever(model.chat(chatRequestCaptor.capture())).thenReturn(chatResponse)
            val systemMessage = SystemMessage.from("You are a helpful assistant")
            val userMessage = UserMessage.from("Say Hello")
            val response =
                model.chatAsync {
                    messages += systemMessage
                    messages += userMessage
                    parameters {
                        temperature = temp
                    }
                }
            assertThat(response).isSameInstanceAs(chatResponse)
            val request = chatRequestCaptor.value
            assertThat(request.messages()).containsExactly(systemMessage, userMessage)
            with(request.parameters()) {
                assertThat(temperature()).isEqualTo(temp)
            }
        }
    }
}
