package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.chat.mock.ChatModelMock
import kotlinx.coroutines.runBlocking
import me.kpavlov.langchain4j.kotlin.model.chat.chatAsync

@Suppress("MagicNumber")
fun main() =
    runBlocking {
        val response =
            ChatModelMock("Hello").chatAsync {
                messages += systemMessage("You are a helpful assistant")
                messages += userMessage("Say Hello")
                parameters {
                    temperature = 0.1
                    modelName = "gpt-4o-mini"
                }
            }
        println("AI Answer: \"${response.aiMessage().text()}\"")
    }
