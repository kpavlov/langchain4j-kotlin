package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.service.UserMessage
import me.kpavlov.langchain4j.kotlin.service.AsyncAiServicesFactory
import me.kpavlov.langchain4j.kotlin.service.createAiService

class AsyncAiServiceExample(
    private val model: ChatModel = OpenAiChatModel
        .builder()
        .modelName("gpt-4o-mini")
        .apiKey(testEnv.get("OPENAI_API_KEY", "demo"))
        .temperature(0.0)
        .build()
) {

    fun interface AsyncAssistant {
        suspend fun askQuestion(
            @UserMessage question: String,
        ): String
    }

    suspend fun callAiService(): String {
        val assistant = createAiService(
            serviceClass = AsyncAssistant::class.java,
            factory = AsyncAiServicesFactory()
        ).chatModel(model)
            .systemMessageProvider { "You are a helpful assistant" }
            .build()

        val response = assistant.askQuestion("How are you?")
        println("AI Answer: \"$response\"")
        return response
    }
}

//fun main() =
//    runBlocking {
//        AsyncAiServiceExample().callAiService()
//    }


