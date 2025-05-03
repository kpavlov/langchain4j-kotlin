package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.mock.ChatModelMock
import dev.langchain4j.service.UserMessage
import kotlinx.coroutines.runBlocking
import me.kpavlov.langchain4j.kotlin.service.AsyncAiServicesFactory
import me.kpavlov.langchain4j.kotlin.service.createAiService

// Use for demo purposes
private val model: ChatModel = ChatModelMock("Hello")

fun interface AsyncAssistant {
    suspend fun askQuestion(
        @UserMessage question: String,
    ): String
}

fun main() =
    runBlocking {
        val assistant = createAiService(
            serviceClass = AsyncAssistant::class.java,
            factory = AsyncAiServicesFactory()
        ).chatModel(model)
            .systemMessageProvider { "You are a helpful assistant" }
            .build()

        val response = assistant.askQuestion("How are you?")
        println("AI Answer: \"$response\"")
    }


