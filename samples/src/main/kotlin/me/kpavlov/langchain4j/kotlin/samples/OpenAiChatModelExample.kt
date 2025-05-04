package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatRequestParameters
import kotlinx.coroutines.runBlocking
import me.kpavlov.langchain4j.kotlin.model.chat.chatAsync

class OpenAiChatModelExample(
    private val model: ChatModel =
        OpenAiChatModel
            .builder()
            .modelName("gpt-4o-mini")
            .apiKey(testEnv.get("OPENAI_API_KEY", "demo"))
            .temperature(0.0)
            .build(),
) {
    suspend fun callChatAsync(): String {
        val response =
            model.chatAsync {
                messages += systemMessage("You are a helpful assistant")
                messages += userMessage("Say Hello")
                parameters(OpenAiChatRequestParameters.builder()) {
                    temperature = 0.1
                    builder.seed(42) // OpenAI specific parameter
                }
            }
        val result = response.aiMessage().text()
        println("AI Answer: \"$result\"")
        return result
    }
}

fun main() {
    runBlocking {
        OpenAiChatModelExample().callChatAsync()
    }
}
// fun main() {
//    runBlocking {
//        OpenAiChatModelExample().callChatAsync()
//    }
// }
