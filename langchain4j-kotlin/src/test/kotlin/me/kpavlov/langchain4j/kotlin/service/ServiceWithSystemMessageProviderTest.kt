package me.kpavlov.langchain4j.kotlin.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.UserMessage
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ServiceWithSystemMessageProviderTest {
    @Test
    fun `Should use SystemMessageProvider`() {
        val chatResponse = ChatResponse.builder().aiMessage(AiMessage("I'm fine, thanks")).build()

        val model =
            object : ChatModel {
                override fun chat(chatRequest: ChatRequest): ChatResponse {
                    chatRequest.messages() shouldHaveSize 2
                    val systemMessage =
                        chatRequest.messages().first {
                            it is SystemMessage
                        } as SystemMessage
                    val userMessage =
                        chatRequest.messages().first {
                            it is dev.langchain4j.data.message.UserMessage
                        } as dev.langchain4j.data.message.UserMessage
                    systemMessage.text() shouldBe "You are helpful assistant"
                    userMessage.singleText() shouldBe "How are you"
                    return chatResponse
                }
            }

        val assistant =
            AiServices
                .builder(Assistant::class.java)
                .systemMessageProvider(
                    object : SystemMessageProvider {
                        override fun getSystemMessage(chatMemoryID: Any): String =
                            "You are helpful assistant"
                    },
                ).chatModel(model)
                .build()

        val response = assistant.askQuestion("How are you")
        assertThat(response).isEqualTo("I'm fine, thanks")
    }

    private interface Assistant {
        fun askQuestion(
            @UserMessage question: String,
        ): String
    }
}
