package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.service.UserMessage
import kotlinx.coroutines.runBlocking
import me.kpavlov.langchain4j.kotlin.service.AsyncAiServicesFactory
import me.kpavlov.langchain4j.kotlin.service.createAiService

private val model: ChatModel = OpenAiChatModel.builder()
    .modelName("gpt-4o-mini")
    .apiKey(testEnv["OPENAI_API_KEY"])
    .build()

fun interface AsyncAssistant {
    suspend fun askQuestion(@UserMessage question: String): String
}

fun main() {
    runBlocking {
        val assistant = createAiService(
            serviceClass = AsyncAssistant::class.java,
            factory = AsyncAiServicesFactory(),
        ).chatModel(model)
            .systemMessageProvider { "You are a helpful software engineer" }
            .build()

        val response = assistant.askQuestion(
            "What's new in Kotlin/AI space in one sentence?"
        )
        println("AI Answer: \"$response\"")
    }
}
