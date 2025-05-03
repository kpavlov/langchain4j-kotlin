package me.kpavlov.langchain4j.kotlin.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.UserName
import dev.langchain4j.service.V
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ServiceWithPromptTemplatesTest {
    @Test
    fun `Should use System and User Prompt Templates`() {
        val chatResponse =
            ChatResponse
                .builder()
                .aiMessage(AiMessage("I'm fine, thanks"))
                .build()

        val model =
            object : ChatModel {
                override fun chat(chatRequest: ChatRequest): ChatResponse {
                    chatRequest.messages() shouldHaveSize 2
                    val systemMessage =
                        chatRequest.messages().first { it is SystemMessage } as SystemMessage
                    val userMessage =
                        chatRequest.messages().first {
                            it is UserMessage
                        } as UserMessage

                    systemMessage.text() shouldBe
                        "You are helpful assistant using chatMemoryID=default"
                    userMessage.singleText() shouldBe "Hello, My friend! How are you?"

                    return chatResponse
                }
            }

        val assistant =
            AiServices
                .builder(Assistant::class.java)
                .systemMessageProvider(
                    TemplateSystemMessageProvider(
                        "prompts/ServiceWithTemplatesTest/default-system-prompt.mustache",
                    ),
                ).chatModel(model)
                .build()

        val response = assistant.askQuestion(userName = "My friend", question = "How are you?")
        assertThat(response).isEqualTo("I'm fine, thanks")
    }

    @Suppress("unused")
    private interface Assistant {
        @dev.langchain4j.service.UserMessage(
            "prompts/ServiceWithTemplatesTest/default-user-prompt.mustache",
        )
        fun askQuestion(
            @UserName userName: String,
            @V("message") question: String,
        ): String
    }
}
