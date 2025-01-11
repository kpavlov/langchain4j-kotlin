package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatRequestParameters
import kotlinx.coroutines.runBlocking
import me.kpavlov.finchly.BaseTestEnvironment
import me.kpavlov.langchain4j.kotlin.model.chat.chatAsync

private val testEnv = BaseTestEnvironment()

// Use for demo purposes
private val model =
    OpenAiChatModel
        .builder()
        .modelName("gpt-4o-mini")
        .apiKey(testEnv.get("OPENAI_API_KEY", "demo"))
        .temperature(0.0)
        .build()

@Suppress("MagicNumber")
fun main() =
    runBlocking {
        val response =
            model.chatAsync {
                messages += systemMessage("You are a helpful assistant")
                messages += userMessage("Say Hello")
                parameters(OpenAiChatRequestParameters.builder()) {
                    temperature(0.1)
                    seed(42) // OpenAI specific parameter
                }
            }
        println("AI Answer: \"${response.aiMessage().text()}\"")
    }
