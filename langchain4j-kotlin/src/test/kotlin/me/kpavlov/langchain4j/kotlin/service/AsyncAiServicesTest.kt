package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class AsyncAiServicesTest {
    @Test
    fun `Should call suspend service`() =
        runTest {
            val chatResponse =
                ChatResponse
                    .builder()
                    .aiMessage(AiMessage("Here is your joke: Hello world!"))
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

                        systemMessage.text() shouldBe "You are a helpful comedian"
                        userMessage.singleText() shouldBe "Tell me a joke"

                        return chatResponse
                    }
                }

            val assistant =
                createAiService(
                    serviceClass = AsyncAssistant::class.java,
                    factory = AsyncAiServicesFactory(),
                ).chatModel(model)
                    .build()

            val response = assistant.askQuestion()
            response shouldBe "Here is your joke: Hello world!"
        }

    @Suppress("unused")
    interface AsyncAssistant {
        @dev.langchain4j.service.SystemMessage("You are a helpful comedian")
        @dev.langchain4j.service.UserMessage("Tell me a joke")
        suspend fun askQuestion(): String
    }
}
