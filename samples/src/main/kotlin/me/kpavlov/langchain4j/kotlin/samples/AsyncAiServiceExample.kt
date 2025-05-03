package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.UserMessage
import kotlinx.coroutines.runBlocking
import me.kpavlov.langchain4j.kotlin.service.AsyncAiServicesFactory
import me.kpavlov.langchain4j.kotlin.service.createAiService


fun interface AsyncAssistant {
    suspend fun askQuestion(@UserMessage question: String): String
}

class AsyncAiServiceExample(
    private val model: ChatModel
) {
    suspend fun callAiService(): String {
        val assistant = createAiService(
            serviceClass = AsyncAssistant::class.java,
            factory = AsyncAiServicesFactory(),
        ).chatModel(model)
            .systemMessageProvider { "You are a helpful software engineer" }
            .build()

        return assistant.askQuestion(
            "What's new in Kotlin/AI space in one sentence?"
        )
    }
}

fun main() {
    runBlocking {
        val response = AsyncAiServiceExample(model).callAiService()
        println("AI Answer: \"$response\"")
    }
}
